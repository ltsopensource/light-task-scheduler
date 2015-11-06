package com.lts.remoting;

import com.lts.remoting.exception.*;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;


/**
 * 远程通信，Client接口
 */
public interface RemotingClient {

    public void start() throws RemotingException;

    /**
     * 同步调用
     */
    public RemotingCommand invokeSync(final String addr, final RemotingCommand request,
                                      final long timeoutMillis) throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException;

    /**
     * 异步调用
     */
    public void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
                            final AsyncCallback asyncCallback) throws InterruptedException, RemotingConnectException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 单向调用
     */
    public void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 注册处理器
     */
    public void registerProcessor(final int requestCode, final RemotingProcessor processor,
                                  final ExecutorService executor);

    /**
     * 注册默认处理器
     */
    public void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);

    public void shutdown();
}
