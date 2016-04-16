package com.github.ltsopensource.nio.processor;

import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.handler.Futures;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioProcessor {

    void accept(SelectionKey key);

    Futures.WriteFuture writeAndFlush(NioChannel channel, Object msg);

    void flush(NioChannel channel);

    void read(NioChannel channel);

    Futures.ConnectFuture connect(SocketAddress remoteAddress);

    void connect(SelectionKey key);
}