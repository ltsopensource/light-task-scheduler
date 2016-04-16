package com.github.ltsopensource.nio.channel;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.nio.codec.Decoder;
import com.github.ltsopensource.nio.codec.Encoder;
import com.github.ltsopensource.nio.config.NioConfig;
import com.github.ltsopensource.nio.handler.Futures;
import com.github.ltsopensource.nio.handler.NioHandler;
import com.github.ltsopensource.nio.idle.IdleInfo;
import com.github.ltsopensource.nio.idle.IdleState;
import com.github.ltsopensource.nio.loop.NioSelectorLoop;
import com.github.ltsopensource.nio.processor.NioProcessor;
import io.netty.util.internal.ThreadLocalRandom;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class NioChannelImpl implements NioChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioChannelImpl.class);

    private final long id;
    private static final AtomicInteger CONN_ID = new AtomicInteger(0);
    private NioProcessor processor;
    private NioSelectorLoop selectorLoop;
    protected Futures.CloseFuture closeFuture = Futures.newCloseFuture();
    private IdleInfo idleInfo;
    private NioConfig config;
    private Decoder decoder;
    private Encoder encoder;
    private final long hashCode = ThreadLocalRandom.current().nextLong();

    protected SocketChannel channel;

    private NioHandler eventHandler;

    public NioChannelImpl(NioSelectorLoop selectorLoop, NioProcessor processor, SocketChannel channel, NioHandler eventHandler, NioConfig config) {
        this.channel = channel;
        this.selectorLoop = selectorLoop;
        this.processor = processor;
        this.eventHandler = eventHandler;
        this.id = CONN_ID.incrementAndGet();
        this.idleInfo = new IdleInfo();
        this.config = config;
        closeFuture.setChannel(this);
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

    public void setLastReadTime(long lastReadTime) {
        idleInfo.setLastReadTime(lastReadTime);
    }

    public void setLastWriteTime(long lastWriteTime) {
        idleInfo.setLastWriteTime(lastWriteTime);
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

    @Override
    public Futures.CloseFuture getCloseFuture() {
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

    public IdleInfo getIdleInfo() {
        return idleInfo;
    }

    public NioConfig getConfig() {
        return config;
    }

    public void fireChannelIdle(IdleState state, long currentTime) {
        switch (state) {
            case BOTH_IDLE:
                idleInfo.setLastBothIdleTime(currentTime);
                break;
            case WRITER_IDLE:
                idleInfo.setLastWriteIdleTime(currentTime);
                break;
            case READER_IDLE:
                idleInfo.setLastReadIdleTime(currentTime);
                break;
        }
        eventHandler().channelIdle(this, state);
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public final int hashCode() {
        return (int) hashCode;
    }

    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

}
