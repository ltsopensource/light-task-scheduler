package com.github.ltsopensource.nio.processor;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.nio.NioException;
import com.github.ltsopensource.nio.channel.ChannelInitializer;
import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.channel.NioChannelImpl;
import com.github.ltsopensource.nio.config.NioServerConfig;
import com.github.ltsopensource.nio.handler.Futures;
import com.github.ltsopensource.nio.handler.NioHandler;
import com.github.ltsopensource.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class NioServerProcessor extends AbstractNioProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioServerProcessor.class);
    private NioServerConfig serverConfig;
    private ServerSocketChannel serverSocketChannel;

    public NioServerProcessor(NioServerConfig serverConfig, NioHandler eventHandler, ChannelInitializer channelInitializer) {
        super(eventHandler, channelInitializer);
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

    public NioChannel doAccept(NioSelectorLoop selectorLoop) {

        SocketChannel socketChannel = null;
        NioChannel channel = null;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            if (serverConfig.getTcpNoDelay() != null) {
                socketChannel.socket().setTcpNoDelay(serverConfig.getTcpNoDelay());
            }
            if (serverConfig.getReceiveBufferSize() != null) {
                socketChannel.socket().setReceiveBufferSize(serverConfig.getReceiveBufferSize());
            }
            if (serverConfig.getKeepAlive() != null) {
                socketChannel.socket().setKeepAlive(serverConfig.getKeepAlive());
            }
            if (serverConfig.getReuseAddress() != null) {
                socketChannel.socket().setReuseAddress(serverConfig.getReuseAddress());
            }
            if (serverConfig.getIpTos() != null) {
                socketChannel.socket().setTrafficClass(serverConfig.getIpTos());
            }
            if (serverConfig.getOobInline() != null) {
                socketChannel.socket().setOOBInline(serverConfig.getOobInline());
            }
            if (serverConfig.getSoLinger() != null) {
                socketChannel.socket().setSoLinger(true, serverConfig.getSoLinger());
            }
            channel = new NioChannelImpl(selectorLoop, this, socketChannel, eventHandler(), serverConfig);
            channelInitializer.initChannel(channel);
            this.idleDetector.addChannel(channel);

            socketChannel.register(selectorLoop.selector(), SelectionKey.OP_READ, channel);

        } catch (IOException e) {
            LOGGER.info("accept the connection IOE", e);
        }

        if (channel != null) {
            eventHandler().channelConnected(channel);
        }
        return channel;
    }

    @Override
    protected NioChannel doConnect(SocketAddress remoteAddress, NioSelectorLoop selectorLoop, Futures.ConnectFuture connectFuture) {
        throw new UnsupportedOperationException();
    }

    public ServerSocketChannel javaChannel() {
        return serverSocketChannel;
    }

    public void register() throws ClosedChannelException {
        javaChannel().register(acceptSelectorLoop().selector(), SelectionKey.OP_ACCEPT);
    }

    public void bind(InetSocketAddress localAddress, NioServerConfig config) throws IOException {
        javaChannel().socket().bind(localAddress, config.getBacklog());
    }

    protected NioSelectorLoop acceptSelectorLoop() {
        return selectorLoop;
    }

    @Override
    public void connect(SelectionKey key) {
        throw new UnsupportedOperationException();
    }
}
