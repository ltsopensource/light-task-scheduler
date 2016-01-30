package com.lts.nio.channel;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.nio.handler.CloseFuture;
import com.lts.nio.handler.NioHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/9/16.
 */
public class NioServerConnection extends AbstractNioConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioServerConnection.class);

    public NioServerConnection(SocketChannel socketChannel,
                               NioHandler eventHandler) {
        super(socketChannel, eventHandler);
    }

    public ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
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

        CloseFuture future = new CloseFuture();
        try {
            javaChannel().socket().close();
            future.setSuccess(true);
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("caught connection exception", e);
            }
            future.setSuccess(false);
            future.setCause(e);
            eventHandler().exceptionCaught(this, e);
        }
        return future;
    }

    @Override
    public boolean isConnected() {
        return javaChannel().socket().isBound();
    }

}
