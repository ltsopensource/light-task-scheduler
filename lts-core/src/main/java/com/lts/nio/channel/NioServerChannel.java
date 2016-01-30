package com.lts.nio.channel;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.nio.handler.CloseFuture;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.processor.NioProcessor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/9/16.
 */
public class NioServerChannel extends AbstractNioChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioServerChannel.class);
    private CloseFuture closeFuture = new CloseFuture();

    public NioServerChannel(NioProcessor processor, SocketChannel socketChannel,
                            NioHandler eventHandler) {
        super(processor, socketChannel, eventHandler);
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
        return null;
    }

    public CloseFuture close() {
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
    public boolean isConnected() {
        return !closeFuture.isDone();
    }

}
