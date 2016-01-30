package com.lts.nio.processor;

import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.nio.NioException;
import com.lts.nio.channel.ChannelContainer;
import com.lts.nio.channel.NioChannel;
import com.lts.nio.channel.WriteQueue;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.handler.WriteFuture;
import com.lts.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public abstract class AbstractNioProcessor implements NioProcessor {

    protected final ChannelContainer channelContainer = new ChannelContainer();

    protected static final Logger LOGGER = LoggerFactory.getLogger(NioProcessor.class);
    private NioHandler eventHandler;
    private NioSelectorLoop selectorLoop;
    private Executor executor;
    private ConcurrentMap<NioChannel, WriteQueue> QUEUE_MAP = new ConcurrentHashMap<NioChannel, WriteQueue>();

    private Encoder encoder;
    private Decoder decoder;

    public AbstractNioProcessor(NioSelectorLoop selectorLoop, NioHandler eventHandler, Encoder encoder, Decoder decoder) {
        this.selectorLoop = selectorLoop;
        this.eventHandler = eventHandler;
        this.encoder = encoder;
        this.decoder = decoder;
        this.executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR,
                new NamedThreadFactory("NioProcessorExecutor-"));
    }

    public WriteFuture writeAndFlush(NioChannel channel, Object msg) {
        return write(channel, msg, true);
    }

    private WriteFuture write(NioChannel channel, Object msg, boolean flush) {

        WriteFuture future = new WriteFuture();

        if (msg == null) {
            future.setSuccess(true);
            future.setMsg("msg is null");
            return future;
        }
        try {
            ByteBuffer buf = encoder.encode(msg);
            QUEUE_MAP.get(channel).offer(new WriteMessage(buf, future));
        } catch (Exception e) {
            throw new NioException("encode msg " + msg + " error", e);
        }

        if (flush) {
            flush(channel);
        }
        SelectionKey key = channel.socketChannel().keyFor(selector());
        key.interestOps(SelectionKey.OP_WRITE);

        return future;
    }

    public void flush(NioChannel channel) {

        WriteQueue queue = QUEUE_MAP.get(channel);

        boolean ok = queue.tryLock();
        if (!ok) {
            // 说明有线程在写
            return;
        }

        try {
            doFlush(queue, channel);
        } finally {
            queue.unlock();
        }
    }

    private void doFlush(final WriteQueue queue, final NioChannel channel) {

        executor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!queue.isEmpty()) {

                        WriteMessage msg = queue.peek();
                        ByteBuffer buf = msg.getMessage();

                        // 已经写的字节数
                        int written = channel.socketChannel().write(buf);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("wrote bytes {}", written);
                        }

                        channel.setLastWriteTime(SystemClock.now());

                        if (buf.remaining() == 0) {

                            queue.poll();

                            WriteFuture writeFuture = msg.getWriteFuture();
                            writeFuture.setSuccess(true);
                            writeFuture.notifyListeners();

                        } else {
                            // 输出socket buffer已经满了 等下一个周期
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("IOE while writing : ", e);
                    eventHandler().exceptionCaught(channel, e);
                }
            }
        });
    }

    public void read(NioChannel channel, ByteBuffer readBuffer) {

        try {
            final int readCount = channel.socketChannel().read(readBuffer);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("read {} bytes", readCount);
            }

            if (readCount < 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("channel closed by the remote peer");
                    channel.close();
                    channelContainer.removeChannel(channel.socketChannel());
                }
            } else if (readCount > 0) {

                readBuffer.flip();

                //  TODO SSL处理

                doMessageReceived(channel, readBuffer);

                readBuffer.clear();
            }

        } catch (IOException e) {
            LOGGER.error("IOE while reading : ", e);
            eventHandler().exceptionCaught(channel, e);
        }
    }

    private void doMessageReceived(final NioChannel channel, final ByteBuffer message) {
        executor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Object msg = decoder.decode(message);
                    eventHandler().messageReceived(channel, msg);
                    channel.setLastReadTime(SystemClock.now());
                } catch (Exception e) {
                    eventHandler().exceptionCaught(channel, e);
                }
            }
        });
    }

    @Override
    public void connect(NioChannel channel) {
        throw new UnsupportedOperationException("sub-class must override this method");
    }

    @Override
    public NioChannel accept() {
        NioChannel channel = doAccept();
        QUEUE_MAP.putIfAbsent(channel, new WriteQueue());
        return channel;
    }

    protected abstract NioChannel doAccept();

    protected NioHandler eventHandler() {
        return this.eventHandler;
    }

    protected Selector selector() {
        return selectorLoop.selector();
    }

    protected Executor executor() {
        return this.executor;
    }
}
