package com.lts.nio;

import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;
import com.lts.nio.config.NioClientConfig;
import com.lts.nio.handler.Futures;
import com.lts.nio.handler.NioHandler;
import com.lts.nio.processor.NioClientProcessor;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class NioClient {

    private NioClientProcessor processor;

    public NioClient(NioClientConfig clientConfig, NioHandler eventHandler, Encoder encoder, Decoder decoder) {
        this.processor = new NioClientProcessor(clientConfig, eventHandler, encoder, decoder);
    }


    public Futures.ConnectFuture connect(SocketAddress remoteAddress) {

        processor.start();

        return processor.connect(remoteAddress);
    }

    public void shutdownGracefully() {
        // TODO
    }
}
