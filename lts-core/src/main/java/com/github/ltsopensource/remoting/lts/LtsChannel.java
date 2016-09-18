package com.github.ltsopensource.remoting.lts;

import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.ChannelHandler;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsChannel implements Channel {

    private com.github.ltsopensource.nio.channel.NioChannel channel;

    public LtsChannel(com.github.ltsopensource.nio.channel.NioChannel channel) {
        this.channel = channel;
    }

    @Override
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public ChannelHandler writeAndFlush(Object msg) {
        return new LtsChannelHandler(channel.writeAndFlush(msg));
    }

    @Override
    public ChannelHandler close() {
        return new LtsChannelHandler(channel.close());
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LtsChannel that = (LtsChannel) o;

        return channel != null ? channel.equals(that.channel) : that.channel == null;

    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }
}
