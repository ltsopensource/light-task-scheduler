package com.lts.job.common.remoting;

import com.lts.job.common.exception.RemotingSendException;
import com.lts.job.common.support.Application;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.RemotingServer;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 * 对 remotingServer 的包装
 */
public class RemotingServerDelegate {

    private RemotingServer remotingServer;

    public RemotingServerDelegate(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }

    public void start() {
        try {
            remotingServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerProcessor(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        remotingServer.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        remotingServer.registerDefaultProcessor(processor, executor);
    }

    public RemotingCommand invokeSync(Channel channel, RemotingCommand request) throws RemotingSendException, RemotingCommandFieldCheckException {
        try {

            request.checkCommandBody();

            return remotingServer.invokeSync(channel, request, Application.Config.getInvokeTimeoutMillis());
        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeAsync(Channel channel, RemotingCommand request, InvokeCallback invokeCallback) throws RemotingCommandFieldCheckException, RemotingSendException {
        try {

            request.checkCommandBody();

            remotingServer.invokeAsync(channel, request, Application.Config.getInvokeTimeoutMillis(), invokeCallback);
        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeOneway(Channel channel, RemotingCommand request) throws RemotingCommandFieldCheckException, RemotingSendException {
        try {

            request.checkCommandBody();

            remotingServer.invokeOneway(channel, request, Application.Config.getInvokeTimeoutMillis());
        } catch (RemotingCommandFieldCheckException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }


    public void shutdown() {
        remotingServer.shutdown();
    }

}
