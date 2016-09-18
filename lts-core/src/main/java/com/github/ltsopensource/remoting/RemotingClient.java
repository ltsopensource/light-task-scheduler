package com.github.ltsopensource.remoting;

import com.github.ltsopensource.remoting.exception.*;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;


/**
 * 远程通信，Client接口
 */
public interface RemotingClient {

    void start() throws RemotingException;

    /**
     * 同步调用
     */
    RemotingCommand invokeSync(final String addr, final RemotingCommand request,
                               final long timeoutMillis) throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException;

    /**
     * 异步调用
     */
    void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
                     final AsyncCallback asyncCallback) throws InterruptedException, RemotingConnectException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 单向调用
     */
    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 注册处理器
     */
    void registerProcessor(final int requestCode, final RemotingProcessor processor,
                           final ExecutorService executor);

    /**
     * 注册默认处理器
     */
    void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);

    void shutdown();
}
