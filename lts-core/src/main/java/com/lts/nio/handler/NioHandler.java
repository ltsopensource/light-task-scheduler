package com.lts.nio.handler;

import com.lts.nio.channel.NioConnection;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioHandler {

    void exceptionCaught(NioConnection connection, Exception cause);

    void messageReceived(NioConnection connection, Object msg);

    void channelConnected(NioConnection connection);

}
