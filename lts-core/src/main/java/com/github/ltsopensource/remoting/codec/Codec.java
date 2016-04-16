package com.github.ltsopensource.remoting.codec;

import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 11/5/15.
 */
public interface Codec {

    RemotingCommand decode(final ByteBuffer byteBuffer) throws Exception;

    ByteBuffer encode(final RemotingCommand remotingCommand) throws Exception;

}
