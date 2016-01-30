package com.lts.nio.handler;

import com.lts.nio.channel.NioConnection;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class EmptyHandler implements NioHandler {

    @Override
    public void exceptionCaught(NioConnection connection, Exception cause) {

    }

    @Override
    public void messageReceived(NioConnection connection, Object msg) {

    }

    @Override
    public void channelConnected(NioConnection connection) {

    }
}
