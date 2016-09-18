package com.github.ltsopensource.remoting;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.remoting.codec.Codec;
import com.github.ltsopensource.remoting.codec.DefaultCodec;
import com.github.ltsopensource.remoting.common.RemotingHelper;
import com.github.ltsopensource.remoting.common.SemaphoreReleaseOnlyOnce;
import com.github.ltsopensource.remoting.common.ServiceThread;
import com.github.ltsopensource.remoting.exception.RemotingSendRequestException;
import com.github.ltsopensource.remoting.exception.RemotingTimeoutException;
import com.github.ltsopensource.remoting.exception.RemotingTooMuchRequestException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingCommandHelper;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.*;


/**
 * Server与Client公用抽象类
 */
public abstract class AbstractRemoting {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemoting.class);

    // 信号量，Oneway情况会使用，防止本地缓存请求过多
    protected final Semaphore semaphoreOneway;

    // 信号量，异步调用情况会使用，防止本地缓存请求过多
    protected final Semaphore semaphoreAsync;

    // 缓存所有对外请求
    protected final ConcurrentHashMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);
    // 注册的各个RPC处理器
    protected final HashMap<Integer/* request code */, Pair<RemotingProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<RemotingProcessor, ExecutorService>>(64);
    protected final RemotingEventExecutor remotingEventExecutor = new RemotingEventExecutor();
    // 默认请求代码处理器
    protected Pair<RemotingProcessor, ExecutorService> defaultRequestProcessor;
    protected final ChannelEventListener channelEventListener;

    public AbstractRemoting(final int permitsOneway, final int permitsAsync, ChannelEventListener channelEventListener) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
        this.channelEventListener = channelEventListener;
    }

    public ChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    public void putRemotingEvent(final RemotingEvent event) {
        this.remotingEventExecutor.putRemotingEvent(event);
    }

    public void processRequestCommand(final Channel channel, final RemotingCommand cmd) {
        final Pair<RemotingProcessor, ExecutorService> matched = this.processorTable.get(cmd.getCode());
        final Pair<RemotingProcessor, ExecutorService> pair =
                null == matched ? this.defaultRequestProcessor : matched;

        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        final RemotingCommand response = pair.getKey().processRequest(channel, cmd);
                        // Oneway形式忽略应答结果
                        if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                            if (response != null) {
                                response.setOpaque(cmd.getOpaque());
                                RemotingCommandHelper.markResponseType(cmd);
                                try {
                                    channel.writeAndFlush(response).addListener(new ChannelHandlerListener() {
                                        @Override
                                        public void operationComplete(Future future) throws Exception {
                                            if (!future.isSuccess()) {
                                                LOGGER.error("response to " + RemotingHelper.parseChannelRemoteAddr(channel) + " failed", future.cause());
                                                LOGGER.error(cmd.toString());
                                                LOGGER.error(response.toString());
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    LOGGER.error("process request over, but response failed", e);
                                    LOGGER.error(cmd.toString());
                                    LOGGER.error(response.toString());
                                }
                            } else {
                                // 收到请求，但是没有返回应答，可能是processRequest中进行了应答，忽略这种情况
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("process request exception", e);
                        LOGGER.error(cmd.toString());

                        if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                            final RemotingCommand response =
                                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_ERROR.code(),//
                                            StringUtils.toString(e));
                            response.setOpaque(cmd.getOpaque());
                            channel.writeAndFlush(response);
                        }
                    }
                }
            };

            try {
                // 这里需要做流控，要求线程池对应的队列必须是有大小限制的
                pair.getValue().submit(run);
            } catch (RejectedExecutionException e) {
                LOGGER.warn(RemotingHelper.parseChannelRemoteAddr(channel) //
                        + ", too many requests and system thread pool busy, RejectedExecutionException " //
                        + pair.getKey().toString() //
                        + " request code: " + cmd.getCode());
                if (!RemotingCommandHelper.isOnewayRPC(cmd)) {
                    final RemotingCommand response =
                            RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_BUSY.code(),
                                    "too many requests and system thread pool busy, please try another server");
                    response.setOpaque(cmd.getOpaque());
                    channel.writeAndFlush(response);
                }
            }
        } else {
            String error = " request type " + cmd.getCode() + " not supported";
            final RemotingCommand response =
                    RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                            error);
            response.setOpaque(cmd.getOpaque());
            channel.writeAndFlush(response);
            LOGGER.error(RemotingHelper.parseChannelRemoteAddr(channel) + error);
        }
    }

    public void processResponseCommand(Channel channel, RemotingCommand cmd) {
        final ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
        if (responseFuture != null) {
            responseFuture.setResponseCommand(cmd);

            responseFuture.release();

            // 异步调用
            if (responseFuture.getAsyncCallback() != null) {
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
                                    LOGGER.warn("execute callback in executor exception, and callback throw", e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        runInThisThread = true;
                        LOGGER.warn("execute callback in executor exception, maybe executor busy", e);
                    }
                } else {
                    runInThisThread = true;
                }

                if (runInThisThread) {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                }
            }
            // 同步调用
            else {
                responseFuture.putResponse(cmd);
            }
        } else {
            LOGGER.warn("receive response, but not matched any request, "
                    + RemotingHelper.parseChannelRemoteAddr(channel));
            LOGGER.warn(cmd.toString());
        }

        responseTable.remove(cmd.getOpaque());
    }

    public void processMessageReceived(Channel channel, final RemotingCommand cmd) throws Exception {
        if (cmd != null) {
            switch (RemotingCommandHelper.getRemotingCommandType(cmd)) {
                case REQUEST_COMMAND:
                    processRequestCommand(channel, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(channel, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    protected abstract ExecutorService getCallbackExecutor();

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
                    LOGGER.error("scanResponseTable, operationComplete exception", e);
                }

                LOGGER.warn("remove timeout request, " + rep);
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
            channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    if (future.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(request.getOpaque());
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                    LOGGER.warn(request.toString());
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
                                final long timeoutMillis, final AsyncCallback asyncCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

            final ResponseFuture responseFuture =
                    new ResponseFuture(request.getOpaque(), timeoutMillis, asyncCallback, once);
            this.responseTable.put(request.getOpaque(), responseFuture);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        if (future.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
                            responseFuture.setSendRequestOK(false);
                        }

                        responseFuture.putResponse(null);
                        responseFuture.executeInvokeCallback();

                        responseTable.remove(request.getOpaque());
                        LOGGER.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        LOGGER.warn(request.toString());
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                LOGGER.warn("invokeAsyncImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreAsync.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    public void invokeOnewayImpl(final Channel channel, final RemotingCommand request,
                                 final long timeoutMillis) throws InterruptedException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException {
        RemotingCommandHelper.markOnewayRPC(request);
        boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelHandlerListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        once.release();
                        if (!future.isSuccess()) {
                            LOGGER.warn("send a request command to channel <" + channel.remoteAddress()
                                    + "> failed.");
                            LOGGER.warn(request.toString());
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                LOGGER.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(RemotingHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                LOGGER.warn("invokeOnewayImpl tryAcquire semaphore timeout, " + timeoutMillis
                        + " waiting thread nums: " + this.semaphoreOneway.getQueueLength());
                LOGGER.warn(request.toString());

                throw new RemotingTimeoutException("tryAcquire timeout(ms) " + timeoutMillis);
            }
        }
    }

    class RemotingEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<RemotingEvent> eventQueue = new LinkedBlockingQueue<RemotingEvent>();
        private final int MaxSize = 10000;

        public void putRemotingEvent(final RemotingEvent event) {
            if (this.eventQueue.size() <= MaxSize) {
                this.eventQueue.add(event);
            } else {
                LOGGER.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
                        event.toString());
            }
        }

        @Override
        public void run() {

            LOGGER.info(this.getServiceName() + " service started");

            final ChannelEventListener listener = AbstractRemoting.this.getChannelEventListener();

            while (!this.isStopped()) {
                try {
                    RemotingEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
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
                    LOGGER.warn(this.getServiceName() + " service has exception. ", e);
                }
            }

            LOGGER.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return RemotingEventExecutor.class.getSimpleName();
        }
    }

    protected Codec getCodec() {
        // TODO 改为SPI
        return new DefaultCodec();
    }

}
