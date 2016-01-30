package com.lts.nio.processor;

import com.lts.nio.channel.NioChannel;
import com.lts.nio.handler.WriteFuture;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioProcessor {

    NioChannel accept();

    WriteFuture writeAndFlush(NioChannel connection, Object msg);

    void flush(NioChannel connection);

    void read(NioChannel connection, ByteBuffer readBuffer);

    void connect(NioChannel connection);

}