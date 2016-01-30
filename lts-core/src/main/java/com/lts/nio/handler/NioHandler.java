package com.lts.nio.handler;

import com.lts.nio.channel.NioChannel;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioHandler {

    void exceptionCaught(NioChannel channel, Exception cause);

    void messageReceived(NioChannel channel, Object msg);

    void channelConnected(NioChannel channel);

}
