package com.github.ltsopensource.nio.codec;

import com.github.ltsopensource.nio.channel.NioChannel;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public interface Decoder {

    List<Object> decode(NioChannel channel, ByteBuffer in) throws Exception;

}
