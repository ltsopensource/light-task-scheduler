package com.github.ltsopensource.core.remoting;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.exception.RemotingSendException;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.AsyncCallback;
import com.github.ltsopensource.remoting.RemotingServer;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.exception.RemotingException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         对 remotingServer 的包装
 */
public class RemotingServerDelegate {

    private RemotingServer remotingServer;
    private AppContext appContext;

    public RemotingServerDelegate(RemotingServer remotingServer, AppContext appContext) {
        this.remotingServer = remotingServer;
        this.appContext = appContext;
    }

    public void start() {
        try {
            remotingServer.start();
        } catch (RemotingException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerProcessor(int requestCode, RemotingProcessor processor,
                                  ExecutorService executor) {
        remotingServer.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
        remotingServer.registerDefaultProcessor(processor, executor);
    }

    public RemotingCommand invokeSync(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            return remotingServer.invokeSync(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeAsync(Channel channel, RemotingCommand request, AsyncCallback asyncCallback)
            throws RemotingSendException {
        try {

            remotingServer.invokeAsync(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeOneway(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            remotingServer.invokeOneway(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void shutdown() {
        remotingServer.shutdown();
    }
}
