package com.lts.remoting;

import com.lts.remoting.exception.RemotingException;
import com.lts.remoting.exception.RemotingSendRequestException;
import com.lts.remoting.exception.RemotingTimeoutException;
import com.lts.remoting.exception.RemotingTooMuchRequestException;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;


/**
 * 远程通信，Server接口
 */
public interface RemotingServer {

    public void start() throws RemotingException;


    /**
     * 注册请求处理器，ExecutorService必须要对应一个队列大小有限制的阻塞队列，防止OOM
     */
    public void registerProcessor(final int requestCode, final RemotingProcessor processor,
                                  final ExecutorService executor);

    /**
     * 注册默认请求处理器
     */
    public void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);


    /**
     * 同步调用
     */
    public RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
                                      final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
            RemotingTimeoutException;

    /**
     * 异步调用
     */
    public void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
                            final AsyncCallback asyncCallback) throws InterruptedException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 单向调用
     */
    public void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
            throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
            RemotingSendRequestException;


    public void shutdown();

}
