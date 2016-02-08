package com.lts.nio.channel;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.nio.handler.Futures;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.loop.NioSelectorLoop;
import com.lts.nio.processor.NioProcessor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class NioTcpChannel implements NioChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioTcpChannel.class);

    private final long id;
    private static final AtomicInteger CONN_ID = new AtomicInteger(0);
    private volatile long lastReadTime;
    private volatile long lastWriteTime;
    private NioProcessor processor;
    private NioSelectorLoop selectorLoop;
    protected Futures.CloseFuture closeFuture = Futures.newCloseFuture();

    protected SocketChannel channel;

    private NioHandler eventHandler;

    public NioTcpChannel(NioSelectorLoop selectorLoop, NioProcessor processor, SocketChannel channel, NioHandler eventHandler) {
        this.channel = channel;
        this.selectorLoop = selectorLoop;
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
    public Futures.WriteFuture writeAndFlush(Object msg) {
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

    public Selector selector() {
        return selectorLoop.selector();
    }

    public Futures.CloseFuture close() {
        try {
            javaChannel().close();
            closeFuture.setSuccess(true);
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("caught connection exception", e);
            }
            closeFuture.setSuccess(false);
            closeFuture.setCause(e);
            eventHandler().exceptionCaught(this, e);
        }
        closeFuture.notifyListeners();
        return closeFuture;
    }

    public boolean isOpen() {
        return javaChannel().isOpen();
    }

    @Override
    public boolean isClosed() {
        return !isOpen();
    }

    @Override
    public SocketAddress remoteAddress() {
        return socketChannel().socket().getRemoteSocketAddress();
    }

    @Override
    public SocketAddress localAddress() {
        return socketChannel().socket().getLocalSocketAddress();
    }

    @Override
    public boolean isConnected() {
        return !closeFuture.isDone();
    }

}
