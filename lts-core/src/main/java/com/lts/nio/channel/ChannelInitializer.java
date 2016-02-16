package com.lts.nio.channel;

import com.lts.nio.codec.Decoder;
import com.lts.nio.codec.Encoder;

/**
 * @author Robert HG (254963746@qq.com) on 2/16/16.
 */
public abstract class ChannelInitializer {

    public void initChannel(NioChannel ch) {
        NioChannelImpl channel = (NioChannelImpl)ch;
        channel.setDecoder(getDecoder());
        channel.setEncoder(getEncoder());
    }

    protected abstract Decoder getDecoder();

    protected abstract Encoder getEncoder();

}
