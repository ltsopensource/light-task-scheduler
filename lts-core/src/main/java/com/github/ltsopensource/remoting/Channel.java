package com.github.ltsopensource.remoting;


import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public interface Channel {

    SocketAddress localAddress();

    SocketAddress remoteAddress();

    ChannelHandler writeAndFlush(Object msg);

    ChannelHandler close();

    boolean isConnected();

    boolean isOpen();

    boolean isClosed();
}
