package com.lts.nio.processor;

import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.nio.NioException;
import com.lts.nio.channel.NioChannel;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.handler.Futures;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public abstract class AbstractNioProcessor implements NioProcessor {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NioProcessor.class);
    private NioHandler eventHandler;
    protected NioSelectorLoop selectorLoop;
    private Executor executor;
    private ConcurrentMap<NioChannel, WriteQueue> QUEUE_MAP = new ConcurrentHashMap<NioChannel, WriteQueue>();
    //    protected NioSelectorLoopPool readWriteSelectorPool;
    private AtomicBoolean started = new AtomicBoolean(false);

    private Encoder encoder;
    private Decoder decoder;

    public AbstractNioProcessor(NioHandler eventHandler, Encoder encoder, Decoder decoder) {
        this.eventHandler = eventHandler;
        this.encoder = encoder;
        this.decoder = decoder;
        this.executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR,
                new NamedThreadFactory("NioProcessorExecutor"));
        this.selectorLoop = new NioSelectorLoop("AcceptSelectorLoop-I/O", this);
//        this.readWriteSelectorPool = new FixedNioSelectorLoopPool(Constants.AVAILABLE_PROCESSOR + 1, "Server", this);
    }

    public Futures.WriteFuture writeAndFlush(NioChannel channel, Object msg) {
        return write(channel, msg, true);
    }

    private Futures.WriteFuture write(NioChannel channel, Object msg, boolean flush) {

        Futures.WriteFuture future = Futures.newWriteFuture();

        if (msg == null) {
            future.setSuccess(true);
            future.setMsg("msg is null");
            future.notifyListeners();
            return future;
        }
        ByteBuffer buf = null;
        try {
            buf = encoder.encode(channel, msg);
            if (buf == null) {
                future.setSuccess(false);
                future.setMsg("encode msg error");
                future.notifyListeners();
                return future;
            }
            QUEUE_MAP.get(channel).offer(new WriteRequest(buf, future));
        } catch (Exception e) {
            throw new NioException("encode msg " + msg + " error", e);
        }
        if (flush) {
            flush(channel);
        }
        return future;
    }

    public void flush(NioChannel channel) {

        WriteQueue queue = QUEUE_MAP.get(channel);

        doFlush(queue, channel);
    }

    private void doFlush(final WriteQueue queue, final NioChannel channel) {

        executor().execute(new Runnable() {
            @Override
            public void run() {

                if (!queue.tryLock()) {
                    // 说明有线程在写
                    return;
                }
                try {
                    while (!queue.isEmpty()) {
                        WriteRequest msg = queue.peek();
                        if (msg == null) {
                            continue;
                        }
                        Futures.WriteFuture writeFuture = msg.getWriteFuture();

                        try {

                            ByteBuffer buf = msg.getMessage();
                            buf.flip();

                            // 已经写的字节数
                            int written = channel.socketChannel().write(buf);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("wrote bytes {}", written);
                            }

                            channel.setLastWriteTime(SystemClock.now());

                            if (buf.remaining() == 0) {

                                queue.poll();

                                writeFuture.setSuccess(true);
                                writeFuture.notifyListeners();

                            } else {
                                // 输出socket buffer已经满了 等下一个周期
                                break;
                            }
                        } catch (Exception e) {
                            LOGGER.error("IOE while writing", e);
                            writeFuture.setSuccess(false);
                            writeFuture.setCause(e);
                            writeFuture.notifyListeners();
                            eventHandler().exceptionCaught(channel, e);
                        }
                    }
                } finally {
                    queue.unlock();
                }
            }
        });
    }

    public void read(NioChannel channel) {
        try {

            // TODO 优化
            ByteBuffer readBuffer = ByteBuffer.allocateDirect(64 * 1024);

            final int readCount = channel.socketChannel().read(readBuffer);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("read {} bytes", readCount);
            }

            if (readCount < 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("channel closed by the remote peer");
                }
                channel.close();
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

    public Futures.ConnectFuture connect(SocketAddress remoteAddress) {
        Futures.ConnectFuture connectFuture = Futures.newConnectFuture();
        NioChannel channel = doConnect(remoteAddress, selectorLoop, connectFuture);
        QUEUE_MAP.putIfAbsent(channel, new WriteQueue());
        return connectFuture;
    }

    public void accept(SelectionKey key) {
        NioChannel channel = doAccept(selectorLoop);
        QUEUE_MAP.putIfAbsent(channel, new WriteQueue());
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            selectorLoop.start();
        }
    }

    protected abstract NioChannel doAccept(NioSelectorLoop selectorLoop);

    protected abstract NioChannel doConnect(SocketAddress remoteAddress, NioSelectorLoop selectorLoop, Futures.ConnectFuture connectFuture);

    protected NioHandler eventHandler() {
        return this.eventHandler;
    }

    protected Executor executor() {
        return this.executor;
    }
}
