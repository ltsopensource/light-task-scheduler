package com.lts.nio.channel;

import com.lts.nio.handler.CloseFuture;
import com.lts.nio.handler.WriteFuture;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioChannel {

    long getId();

    SocketAddress remoteAddress();

    WriteFuture writeAndFlush(Object msg);

    CloseFuture close();

    boolean isConnected();

    boolean isOpen();

    boolean isClosed();

    SocketChannel socketChannel();

    void setLastReadTime(long lastReadTime);

    void setLastWriteTime(long lastWriteTime);

}
