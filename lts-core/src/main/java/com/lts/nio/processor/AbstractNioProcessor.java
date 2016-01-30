package com.lts.nio.processor;

import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.nio.NioException;
import com.lts.nio.channel.ConnectionContainer;
import com.lts.nio.channel.NioConnection;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.handler.WriteFuture;
import com.lts.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class AbstractNioProcessor implements NioProcessor {

    protected final ConnectionContainer container = new ConnectionContainer();

    protected static final Logger LOGGER = LoggerFactory.getLogger(NioProcessor.class);
    private NioHandler eventHandler;
    private NioSelectorLoop selectorLoop;
    private Executor executor;

    private ConcurrentLinkedQueue<WriteMessage> writeQueue = new ConcurrentLinkedQueue<WriteMessage>();

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

    public WriteFuture write(NioConnection connection, Object msg) {

        WriteFuture future = new WriteFuture();

        if (msg == null) {
            future.setSuccess(true);
            future.setMsg("msg is null");
            return future;
        }
        try {
            ByteBuffer buf = encoder.encode(msg);
            writeQueue.offer(new WriteMessage(buf, future));
        } catch (Exception e) {
            throw new NioException("encode msg " + msg + " error", e);
        }

        SelectionKey key = connection.socketChannel().keyFor(selector());
        key.interestOps(SelectionKey.OP_READ);

        return future;
    }

    @Override
    public void write(SelectionKey key) {

        NioConnection connection = container.getConnection(key.channel());

        try {

            while (!writeQueue.isEmpty()) {

                WriteMessage msg = writeQueue.peek();

                ByteBuffer buf = msg.getMessage();

                // 已经写的字节数
                int written = connection.socketChannel().write(buf);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("wrote bytes {}", written);
                }

                connection.setLastWriteTime(SystemClock.now());

                if (buf.remaining() == 0) {

                    writeQueue.poll();

                    WriteFuture writeFuture = msg.getWriteFuture();
                    writeFuture.setSuccess(true);
                    writeFuture.notifyListeners();

                } else {
                    // 输出socket buffer已经满了 等下一个周期
                    break;
                }
            }

            if (writeQueue.isEmpty()) {

                // 已经写完了队列中所有的数据,重新关注下读
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            LOGGER.error("IOE while writing : ", e);
            eventHandler().exceptionCaught(connection, e);
        }
    }

    @Override
    public void read(SelectionKey key, ByteBuffer readBuffer) {

        NioConnection connection = container.getConnection(key.channel());

        try {
            final int readCount = connection.socketChannel().read(readBuffer);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("read {} bytes", readCount);
            }

            if (readCount < 0) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("connection closed by the remote peer");
                    connection.close();
                    container.removeConnection(connection.socketChannel());
                }
            } else if (readCount > 0) {

                readBuffer.flip();

                //  TODO SSL处理

                doMessageReceived(connection, readBuffer);

                readBuffer.clear();
            }

        } catch (IOException e) {
            LOGGER.error("IOE while reading : ", e);
            eventHandler().exceptionCaught(connection, e);
        }
    }

    private void doMessageReceived(final NioConnection connection, ByteBuffer message) {

        Executor executor = executor();

        Object msg = null;
        try {
            msg = decoder.decode(message);
        } catch (Exception e) {
            throw new NioException("decode error", e);
        }
        if (msg != null) {
            final Object finalMsg = msg;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        eventHandler().messageReceived(connection, finalMsg);
                    } catch (Exception e) {
                        eventHandler().exceptionCaught(connection, e);
                    }
                }
            });
        }
        connection.setLastReadTime(SystemClock.now());
    }

    @Override
    public void connect(SelectionKey key) {
        // do noting , 留给子类去覆盖
    }

    @Override
    public void accept(SelectionKey key, Selector selector) {
        // do noting , 留给子类去覆盖
    }

    protected NioHandler eventHandler() {
        return this.eventHandler;
    }

    protected Selector selector() {
        return selectorLoop.selector();
    }

    protected Executor executor(){
        return this.executor;
    }
}
