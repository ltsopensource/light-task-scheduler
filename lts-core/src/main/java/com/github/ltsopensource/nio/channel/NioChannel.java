package com.github.ltsopensource.nio.channel;

import com.github.ltsopensource.nio.codec.Decoder;
import com.github.ltsopensource.nio.codec.Encoder;
import com.github.ltsopensource.nio.handler.Futures;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioChannel {

    long getId();

    SocketAddress remoteAddress();

    SocketAddress localAddress();

    Futures.WriteFuture writeAndFlush(Object msg);

    Futures.CloseFuture close();

    Futures.CloseFuture getCloseFuture();

    boolean isConnected();

    boolean isOpen();

    boolean isClosed();

    SocketChannel socketChannel();

    void setLastReadTime(long lastReadTime);

    void setLastWriteTime(long lastWriteTime);

    Decoder getDecoder();

    Encoder getEncoder();
}
