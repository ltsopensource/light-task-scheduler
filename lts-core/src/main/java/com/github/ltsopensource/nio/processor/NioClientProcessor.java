package com.github.ltsopensource.nio.processor;

import com.github.ltsopensource.nio.NioException;
import com.github.ltsopensource.nio.channel.ChannelInitializer;
import com.github.ltsopensource.nio.channel.NioChannel;
import com.github.ltsopensource.nio.channel.NioChannelImpl;
import com.github.ltsopensource.nio.config.NioClientConfig;
import com.github.ltsopensource.nio.handler.Futures;
import com.github.ltsopensource.nio.handler.NioHandler;
import com.github.ltsopensource.nio.loop.NioSelectorLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 2/2/16.
 */
public class NioClientProcessor extends AbstractNioProcessor {

    private NioClientConfig clientConfig;
    private NioChannelImpl channel;

    public NioClientProcessor(NioClientConfig clientConfig, NioHandler eventHandler, ChannelInitializer channelInitializer) {
        super(eventHandler, channelInitializer);
        this.clientConfig = clientConfig;
    }

    @Override
    protected NioChannel doAccept(NioSelectorLoop selectorLoop) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected NioChannel doConnect(SocketAddress remoteAddress, NioSelectorLoop selectorLoop, Futures.ConnectFuture connectFuture) {

        SocketChannel socketChannel = newSocket();
        try {
            socketChannel.socket().setSoTimeout(clientConfig.getConnectTimeout());

            socketChannel.configureBlocking(false);

            if (clientConfig.getTcpNoDelay() != null) {
                socketChannel.socket().setTcpNoDelay(clientConfig.getTcpNoDelay());
            }
            if (clientConfig.getReceiveBufferSize() != null) {
                socketChannel.socket().setReceiveBufferSize(clientConfig.getReceiveBufferSize());
            }
            if (clientConfig.getKeepAlive() != null) {
                socketChannel.socket().setKeepAlive(clientConfig.getKeepAlive());
            }
            if (clientConfig.getReuseAddress() != null) {
                socketChannel.socket().setReuseAddress(clientConfig.getReuseAddress());
            }
            if (clientConfig.getIpTos() != null) {
                socketChannel.socket().setTrafficClass(clientConfig.getIpTos());
            }
            if (clientConfig.getOobInline() != null) {
                socketChannel.socket().setOOBInline(clientConfig.getOobInline());
            }
            if (clientConfig.getSoLinger() != null) {
                socketChannel.socket().setSoLinger(true, clientConfig.getSoLinger());
            }

        } catch (IOException e) {
            throw new NioException("connect IOE", e);
        }

        this.channel = new NioChannelImpl(selectorLoop, this, socketChannel, eventHandler(), clientConfig);

        try {
            socketChannel.connect(remoteAddress);
            socketChannel.register(this.selectorLoop.selector(), SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            connectFuture.setCause(e);
            connectFuture.setSuccess(false);
            connectFuture.notifyListeners();
            this.channel = null;
            return null;
        }

        channelInitializer.initChannel(this.channel);
        idleDetector.addChannel(channel);
        connectFuture.setSuccess(true);
        connectFuture.setChannel(channel);
        connectFuture.notifyListeners();
        return channel;
    }

    private static SocketChannel newSocket() {
        try {
            return SocketChannel.open();
        } catch (IOException e) {
            throw new NioException("can't create a new socket, out of file descriptors ?", e);
        }
    }

    @Override
    public void connect(SelectionKey key) {
        try {
            channel.socketChannel().finishConnect();
            key.attach(channel);
        } catch (IOException e) {
            eventHandler().exceptionCaught(channel, e);
            key.cancel();
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
    }

}
