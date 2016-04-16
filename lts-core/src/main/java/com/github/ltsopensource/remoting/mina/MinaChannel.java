package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.ChannelHandler;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/4/15.
 */
public class MinaChannel implements Channel {

    private IoSession session;

    public MinaChannel(IoSession session) {
        this.session = session;
    }

    @Override
    public SocketAddress localAddress() {
        return session.getLocalAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return session.getRemoteAddress();
    }

    @Override
    public ChannelHandler writeAndFlush(Object msg) {
        WriteFuture writeFuture = session.write(msg);
        return new MinaChannelHandler(writeFuture);
    }

    @Override
    public ChannelHandler close() {
        CloseFuture closeFuture = session.close(false);
        return new MinaChannelHandler(closeFuture);
    }

    @Override
    public boolean isConnected() {
        return session.isConnected();
    }

    @Override
    public boolean isOpen() {
        return session.isConnected();
    }

    @Override
    public boolean isClosed() {
        return session.isClosing();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinaChannel that = (MinaChannel) o;

        return !(session != null ? !session.equals(that.session) : that.session != null);

    }

    @Override
    public int hashCode() {
        return session != null ? session.hashCode() : 0;
    }
}
