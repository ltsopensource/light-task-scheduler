package com.lts.nio;

import com.lts.core.constant.Constants;
import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.config.NioServerConfig;
import com.lts.nio.handler.EmptyHandler;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.loop.FixedNioSelectorLoopPool;
import com.lts.nio.loop.NioSelectorLoop;
import com.lts.nio.loop.NioSelectorLoopPool;
import com.lts.nio.processor.NioServerProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/9/16.
 */
public class NioServer {

    private NioServerConfig serverConfig;
    private NioSelectorLoop acceptSelectorLoop;
    private NioServerProcessor processor;
    private NioSelectorLoopPool readWriteSelectorPool;
    private NioHandler eventHandler;

    public NioServer(NioServerConfig serverConfig, NioHandler eventHandler, Encoder encoder, Decoder decoder) {
        this.serverConfig = serverConfig;
        setEventHandler(eventHandler);
//        this.connection = new NioServerConnection(this.eventHandler);
        this.acceptSelectorLoop = new NioSelectorLoop("AcceptSelectorLoop-I/O", processor);
        this.processor = new NioServerProcessor(this.acceptSelectorLoop, serverConfig, this.eventHandler, encoder, decoder);
        this.readWriteSelectorPool = new FixedNioSelectorLoopPool(Constants.AVAILABLE_PROCESSOR + 1, "Server", processor);
    }

    private void setEventHandler(NioHandler eventHandler) {
        if (eventHandler == null) {
            eventHandler = new EmptyHandler();
        }
        this.eventHandler = eventHandler;
    }

    public void bind(InetSocketAddress localAddress) {

        // 初始化
        init();

        // 注册
        try {
            processor.register();
        } catch (ClosedChannelException e) {
            throw new NioException("register channel error:" + e.getMessage(), e);
        }

        // 绑定
        try {
            processor.bind(localAddress, serverConfig);
        } catch (IOException e) {
            throw new NioException("bind channel error:" + e.getMessage(), e);
        }

        acceptSelectorLoop.start();
    }

    private void init() {

        ServerSocketChannel socketChannel = processor.javaChannel();

        ServerSocket javaSocket = socketChannel.socket();

        try {
            if (serverConfig.getReceiveBufferSize() != null) {
                javaSocket.setReceiveBufferSize(serverConfig.getReceiveBufferSize());
            }
            if (serverConfig.getReuseAddress() != null) {
                javaSocket.setReuseAddress(serverConfig.getReuseAddress());
            }
        } catch (SocketException e) {
            throw new NioException("config channel error:" + e.getMessage(), e);
        }
    }

}
