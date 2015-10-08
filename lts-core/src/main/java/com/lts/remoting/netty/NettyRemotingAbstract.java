package com.lts.remoting.netty;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.remoting.ChannelEventListener;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.common.Pair;
import com.lts.remoting.common.RemotingHelper;
import com.lts.remoting.common.SemaphoreReleaseOnlyOnce;
import com.lts.remoting.common.ServiceThread;
import com.lts.remoting.exception.RemotingSendRequestException;
import com.lts.remoting.exception.RemotingTimeoutException;
import com.lts.remoting.exception.RemotingTooMuchRequestException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;


/**
 * Server与Client公用抽象类
 */
public abstract class NettyRemotingAbstract {
    private static final Logger plog = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    // 信号量，Oneway情况会使用，防止本地Netty缓存请求过多
    protected final Semaphore semaphoreOneway;

    // 信号量，异步调用情况会使用，防止本地Netty缓存请求过多
    protected final Semaphore semaphoreAsync;

    // 缓存所有对外请求
    protected final ConcurrentHashMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);
    // 注册的各个RPC处理器
    protected final HashMap<Integer/* request code */, Pair<NettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>>(64);
    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor();
    // 默认请求代码处理器
    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;


    public NettyRemotingAbstract(final int permitsOneway, final int permitsAsync) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
    }

    public abstract ChannelEventListener getChannelEventListener();

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecutor.putNettyEvent(event);
    }

    public void processRequestCommand(final ChannelHandlerContext ctx, final RemotingCommand cmd) {
        final Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<NettyRequestProcessor, ExecutorService> pair =
                null == matched ? this.defaultRequestProcessor : matched;

        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        final RemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
                        // Oneway形式忽略应答结果
                        if (!cmd.isOnewayRPC()) {
                            if (response != null) {
                                response.setOpaque(cmd.getOpaque());
                                response.markResponseType();
                                try {
                                    ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) throws Exception {
                                            if (!future.isSuccess()) {
                                                plog.error("response to " + RemotingHelper.parseChannelRemoteAddr(future.channel()) + " failed", future.cause());
                                                plog.error(cmd.toString());
                                                plog.error(response.toString());
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    plog.error("process request over, but response failed", e);
                                    plog.error(cmd.toString());
                                    plog.error(response.toString());
                                }
                            } else {
                                // 收到请求，但是没有返回应答，可能是processRequest中进行了应答，忽略这种情况
                            }
                        }
                    } catch (Exception e) {
                        plog.error("process request exception", e);
                        plog.error(cmd.toString());

                        if (!cmd.isOnewayRPC()) {
                            final RemotingCommand response =
                                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_ERROR.code(),//
                                            RemotingHelper.exceptionSimpleDesc(e));
                            response.setOpaque(cmd.getOpaque());
                            ctx.writeAndFlush(response);
                        }
                    }
                }
            };

            try {
                // 这里需要做流控，要求线程池对应的队列必须是有大小限制的
                pair.getObject2().submit(run);
            } catch (RejectedExecutionException e) {
                plog.warn(RemotingHelper.parseChannelRemoteAddr(ctx.channel()) //
                        + ", too many requests and system thread pool busy, RejectedExecutionException " //
                        + pair.getObject2().toString() //
                        + " request code: " + cmd.getCode());
                if (!cmd.isOnewayRPC()) {
                    final RemotingCommand response =
                            RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_BUSY.code(),
                                    "too many requests and system thread pool busy, please try another server");
                    response.setOpaque(cmd.getOpaque());
                    ctx.writeAndFlush(response);
                }
            }
        } else {
            String error = " request type " + cmd.getCode() + " not supported";
            final RemotingCommand response =
                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                            error);
            response.setOpaque(cmd.getOpaque());
            ctx.writeAndFlush(response);
            plog.error(RemotingHelper.parseChannelRemoteAddr(ctx.channel()) + error);
        }
    }

    public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {
        final ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);

            responseFuture.release();

            // 异步调用
            if (responseFuture.getInvokeCallback() != null) {
                boolean runInThisThread = false;
                ExecutorService executor = this.getCallbackExecutor();
                if (executor != null) {
                    try {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    responseFuture.executeInvokeCallback();
                                } catch (Exception e) {
                                    plog.warn("excute callback in executor exception, and callback throw", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        plog.warn("excute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Exception e) {
                        plog.warn("", e);
                    }
                }
            }
            // 同步调用
            else {
                responseFuture.putResponse(cmd);
            }
        } else {
            plog.warn("receive response, but not matched any request, "
                    + RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
            plog.warn(cmd.toString());
        }

        responseTable.remove(cmd.getOpaque());
    }

    public void processMessageReceived(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
        final RemotingCommand cmd = msg;
        if (cmd != null) {
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequestCommand(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    abstract public ExecutorService getCallbackExecutor();

    public void scanResponseTable() {
        Iterator<Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= SystemClock.now()) {
                it.remove();
                rep.release();
                try {
                    rep.executeInvokeCallback();
                } catch (Exception e) {
                    plog.error("scanResponseTable, operationComplete exception", e);
                }

                plog.warn("remove timeout request, " + rep);
            }
        }
    }

    public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,
                                          final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException {
        try {
            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, null, null);
            this.responseTable.put(request.getOpaque(), responseFuture);
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(request.getOpaque());
                    responseFuture.setCause(f.cause());
                    responseFuture.putResponse(null);
                    plog.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                    plog.warn(request.toString());
                }
            });

            RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                // 发送请求成功，读取应答超时
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(RemotingHelper.parseChannelRemoteAddr(channel),
                            timeoutMillis, responseFuture.getCause());
                }
                // 发送请求失败
                else {
                    throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel),
                            responseFuture.getCause());
                }
            }

            return responseCommand;
        } finally {
            this.responseTable.remove(request.getOpaque());
        }
    }

    public void invokeAsyncImpl(final Channel channel, final RemotingCommand request,
                                final long timeoutMillis, final InvokeCallback invokeCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, invokeCallback, once);
            this.responseTable.put(request.getOpaque(), responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        if (f.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseFuture.executeInvokeCallback();

                        responseTable.remove(request.getOpaque());
                        plog.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        plog.warn(request.toString());

                    }
                });
            } catch (Exception e) {
                once.release();
                plog.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                plog.warn("invokeAsyncImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreAsync.getQueueLength());
                plog.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final RemotingCommand request,
                                 final long timeoutMillis) throws InterruptedException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException {
        request.markOnewayRPC();
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {
                        once.release();
                        if (!f.isSuccess()) {
                            plog.warn("send a request command to channel <" + channel.remoteAddress()
                                    + "> failed.");
                            plog.warn(request.toString());
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                plog.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                plog.warn("invokeOnewayImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreOneway.getQueueLength());
                plog.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    class NettyEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
        private final int MaxSize = 10000;


        public void putNettyEvent(final NettyEvent event) {
            if (this.eventQueue.size() <= MaxSize) {
                this.eventQueue.add(event);
            } else {
                plog.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
                        event.toString());
            }
        }


        @Override
        public void run() {
            plog.info(this.getServiceName() + " service started");

            final ChannelEventListener listener = NettyRemotingAbstract.this.getChannelEventListener();

            while (!this.isStoped()) {
                try {
                    NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        switch (event.getType()) {
                            case ALL_IDLE:
                                listener.onChannelIdle(IdleState.ALL_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case WRITER_IDLE:
                                listener.onChannelIdle(IdleState.WRITER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case READER_IDLE:
                                listener.onChannelIdle(IdleState.READER_IDLE, event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;

                        }
                    }
                } catch (Exception e) {
                    plog.warn(this.getServiceName() + " service has exception. ", e);
                }
            }

            plog.info(this.getServiceName() + " service end");
        }


        @Override
        public String getServiceName() {
            return NettyEventExecutor.class.getSimpleName();
        }
    }


}
