package com.lts.nio.processor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @author Robert HG (254963746@qq.com) on 1/24/16.
 */
public interface NioProcessor {

    void accept(SelectionKey key, Selector selector);

    void write(SelectionKey key);

    void read(SelectionKey key, ByteBuffer readBuffer);

    void connect(SelectionKey key);
}