package com.lts.nio.codec;

import com.lts.nio.channel.NioChannel;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public interface Encoder {

    ByteBuffer encode(NioChannel channel, Object msg);

}
