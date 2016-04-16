package com.github.ltsopensource.nio;

import com.github.ltsopensource.nio.channel.ChannelInitializer;
import com.github.ltsopensource.nio.config.NioClientConfig;
import com.github.ltsopensource.nio.handler.Futures;
import com.github.ltsopensource.nio.handler.NioHandler;
import com.github.ltsopensource.nio.processor.NioClientProcessor;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class NioClient {

    private NioClientProcessor processor;

    public NioClient(NioClientConfig clientConfig, NioHandler eventHandler, ChannelInitializer channelInitializer) {
        this.processor = new NioClientProcessor(clientConfig, eventHandler, channelInitializer);
    }

    public Futures.ConnectFuture connect(SocketAddress remoteAddress) {

        processor.start();

        return processor.connect(remoteAddress);
    }

    public void shutdownGracefully() {
        // TODO
    }
}
