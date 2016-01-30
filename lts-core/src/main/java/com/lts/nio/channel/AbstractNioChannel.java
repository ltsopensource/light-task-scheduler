package com.lts.nio.channel;

import com.lts.nio.handler.NioHandler;
import com.lts.nio.handler.WriteFuture;
import com.lts.nio.processor.NioProcessor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public abstract class AbstractNioChannel implements NioChannel {

    private final long id;
    private static final AtomicInteger CONN_ID = new AtomicInteger(0);
    private volatile long lastReadTime;
    private volatile long lastWriteTime;
    private NioProcessor processor;

    protected SocketChannel channel;

    private NioHandler eventHandler;

    public AbstractNioChannel(NioProcessor processor, SocketChannel channel, NioHandler eventHandler) {
        this.channel = channel;
        this.processor = processor;
        this.eventHandler = eventHandler;
        this.id = CONN_ID.incrementAndGet();
    }

    public SelectableChannel javaChannel() {
        return channel;
    }

    public SocketChannel socketChannel() {
        return channel;
    }

    public NioHandler eventHandler() {
        return eventHandler;
    }

    public long getId() {
        return id;
    }

    @Override
    public WriteFuture writeAndFlush(Object msg) {
        return processor.writeAndFlush(this, msg);
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(long lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
    }

}
