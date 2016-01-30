package com.lts.nio.channel;

import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.handler.WriteFuture;
import com.lts.nio.processor.AbstractNioProcessor;
import com.lts.nio.processor.NioProcessor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public abstract class AbstractNioConnection implements NioConnection {

    private final long id;
    private static final AtomicInteger CONN_ID = new AtomicInteger(0);
    private NioProcessor processor;
    private volatile long lastReadTime;
    private volatile long lastWriteTime;

    protected SocketChannel channel;

    private NioHandler eventHandler;

    public AbstractNioConnection(SocketChannel channel, NioHandler eventHandler) {
        this.channel = channel;
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

    public void setProcessor(NioProcessor processor) {
        this.processor = processor;
    }

    @Override
    public WriteFuture writeAndFlush(Object msg) {
        return ((AbstractNioProcessor) processor).write(this, msg);
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
