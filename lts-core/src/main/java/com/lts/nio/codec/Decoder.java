package com.lts.nio.codec;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public interface Decoder {

    Object decode(ByteBuffer in) throws Exception;

}
