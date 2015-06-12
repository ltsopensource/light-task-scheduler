package com.lts.core.remoting;

import com.lts.core.Application;
import com.lts.core.exception.RemotingSendException;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.RemotingServer;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         对 remotingServer 的包装
 */
public class RemotingServerDelegate {

    private RemotingServer remotingServer;
    private Application application;

    public RemotingServerDelegate(RemotingServer remotingServer, Application application) {
        this.remotingServer = remotingServer;
        this.application = application;
    }

    public void start() {
        try {
            remotingServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerProcessor(int requestCode, NettyRequestProcessor processor,
                                  ExecutorService executor) {
        remotingServer.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        remotingServer.registerDefaultProcessor(processor, executor);
    }

    public RemotingCommand invokeSync(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            return remotingServer.invokeSync(channel, request,
                    application.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeAsync(Channel channel, RemotingCommand request, InvokeCallback invokeCallback)
            throws RemotingSendException {
        try {

            remotingServer.invokeAsync(channel, request,
                    application.getConfig().getInvokeTimeoutMillis(), invokeCallback);
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeOneway(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            remotingServer.invokeOneway(channel, request,
                    application.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }


    public void shutdown() {
        remotingServer.shutdown();
    }
}
