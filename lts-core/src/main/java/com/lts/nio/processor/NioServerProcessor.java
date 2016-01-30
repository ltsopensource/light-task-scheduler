package com.lts.nio.processor;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.nio.NioException;
import com.lts.nio.channel.NioConnection;
import com.lts.nio.channel.NioServerConnection;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.config.NioServerConfig;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class NioServerProcessor extends AbstractNioProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioServerProcessor.class);
    private NioServerConfig serverConfig;
    private ServerSocketChannel serverSocketChannel;

    public NioServerProcessor(NioSelectorLoop selectorLoop, NioServerConfig serverConfig, NioHandler eventHandler, Encoder encoder, Decoder decoder) {
        super(selectorLoop, eventHandler, encoder, decoder);
        this.serverConfig = serverConfig;
        this.serverSocketChannel = newSocket();
    }

    private static ServerSocketChannel newSocket() {
        try {
            ServerSocketChannel ch = ServerSocketChannel.open();
            ch.configureBlocking(false);
            return ch;
        } catch (IOException e) {
            throw new NioException("Open a server socket error:" + e.getMessage(), e);
        }
    }

    public void accept(SelectionKey key, Selector selector) {

        SocketChannel socketChannel = null;
        NioConnection connection = null;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            socketChannel.socket().setTcpNoDelay(serverConfig.getTcpNoDelay());
            socketChannel.socket().setReceiveBufferSize(serverConfig.getReceiveBufferSize());
            socketChannel.socket().setKeepAlive(serverConfig.getKeepAlive());
            socketChannel.socket().setReuseAddress(serverConfig.getReuseAddress());
            socketChannel.socket().setTrafficClass(serverConfig.getIpTos());

            socketChannel.register(selector, SelectionKey.OP_READ);

            connection = new NioServerConnection(socketChannel, eventHandler());

        } catch (IOException e) {
            LOGGER.info("accept the connection IOE", e);
        }

        if (connection != null) {
            container.addConnection(socketChannel, connection);
        }

        eventHandler().channelConnected(connection);
    }

    public ServerSocketChannel javaChannel() {
        return serverSocketChannel;
    }

    public void register() throws ClosedChannelException {
        javaChannel().register(selector(), SelectionKey.OP_ACCEPT);
    }

    public void bind(InetSocketAddress localAddress, NioServerConfig config) throws IOException {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }

}
