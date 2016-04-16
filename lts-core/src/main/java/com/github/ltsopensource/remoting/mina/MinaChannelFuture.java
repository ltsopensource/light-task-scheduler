package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.ChannelFuture;
import org.apache.mina.core.future.ConnectFuture;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaChannelFuture implements ChannelFuture {

    private ConnectFuture connectFuture;

    public MinaChannelFuture(ConnectFuture connectFuture) {
        this.connectFuture = connectFuture;
    }

    @Override
    public boolean isConnected() {
        return connectFuture.isConnected() && connectFuture.getSession().isConnected();
    }

    @Override
    public Channel getChannel() {
        return new MinaChannel(connectFuture.getSession());
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return connectFuture.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public boolean isDone() {
        return connectFuture.isDone();
    }

    @Override
    public Throwable cause() {
        return connectFuture.getException();
    }
}
