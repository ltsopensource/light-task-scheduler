package com.github.ltsopensource.remoting;

import com.github.ltsopensource.core.domain.Pair;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.remoting.exception.RemotingException;
import com.github.ltsopensource.remoting.exception.RemotingSendRequestException;
import com.github.ltsopensource.remoting.exception.RemotingTimeoutException;
import com.github.ltsopensource.remoting.exception.RemotingTooMuchRequestException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Remoting服务端实现
 */
public abstract class AbstractRemotingServer extends AbstractRemoting implements RemotingServer {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemotingServer.class);

    protected final RemotingServerConfig remotingServerConfig;
    // 处理Callback应答器
    private final ExecutorService publicExecutor;
    // 定时器
    private final Timer timer = new Timer("ServerHouseKeepingService", true);

    public AbstractRemotingServer(final RemotingServerConfig remotingServerConfig,
                                  final ChannelEventListener channelEventListener) {
        super(remotingServerConfig.getServerOnewaySemaphoreValue(),
                remotingServerConfig.getServerAsyncSemaphoreValue(), channelEventListener);
        this.remotingServerConfig = remotingServerConfig;

        int publicThreadNums = remotingServerConfig.getServerCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new NamedThreadFactory("RemotingServerPublicExecutor", true));
    }

    @Override
    public final void start() throws RemotingException {

        serverStart();

        if (channelEventListener != null) {
            this.remotingEventExecutor.start();
        }

        // 每隔1秒扫描下异步调用超时情况
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    AbstractRemotingServer.this.scanResponseTable();
                } catch (Exception e) {
                    LOGGER.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
    }

    protected abstract void serverStart() throws RemotingException;

    @Override
    public void registerProcessor(int requestCode, RemotingProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (null == executor) {
            executorThis = this.publicExecutor;
        }

        Pair<RemotingProcessor, ExecutorService> pair =
                new Pair<RemotingProcessor, ExecutorService>(processor, executorThis);
        this.processorTable.put(requestCode, pair);
    }

    @Override
    public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<RemotingProcessor, ExecutorService>(processor, executor);
    }

    @Override
    public RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
                                      final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException {
        return this.invokeSyncImpl(channel, request, timeoutMillis);
    }

    @Override
    public void invokeAsync(Channel channel, RemotingCommand request, long timeoutMillis,
                            AsyncCallback asyncCallback) throws InterruptedException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException {
        this.invokeAsyncImpl(channel, request, timeoutMillis, asyncCallback);
    }

    @Override
    public void invokeOneway(Channel channel, RemotingCommand request, long timeoutMillis)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
            RemotingSendRequestException {
        this.invokeOnewayImpl(channel, request, timeoutMillis);
    }

    @Override
    public void shutdown() {
        try {
            if (this.timer != null) {
                this.timer.cancel();
            }

            if (this.remotingEventExecutor != null) {
                this.remotingEventExecutor.shutdown();
            }

            serverShutdown();

        } catch (Exception e) {
            LOGGER.error("RemotingServer shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                LOGGER.error("RemotingServer shutdown exception, ", e);
            }
        }
    }

    protected abstract void serverShutdown() throws RemotingException;

    @Override
    protected ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }
}
