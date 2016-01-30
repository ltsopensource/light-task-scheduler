package com.lts.nio.handler;

import com.lts.nio.channel.NioChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public class EmptyHandler implements NioHandler {

    @Override
    public void exceptionCaught(NioChannel connection, Exception cause) {

    }

    @Override
    public void messageReceived(NioChannel connection, Object msg) {

    }

    @Override
    public void channelConnected(NioChannel connection) {

    }
}
