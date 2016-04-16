package com.github.ltsopensource.nio.processor;

import com.github.ltsopensource.nio.handler.Futures;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class WriteRequest {

    private ByteBuffer message;
    private Futures.WriteFuture writeFuture;

    public WriteRequest(ByteBuffer message, Futures.WriteFuture writeFuture) {
        this.message = message;
        this.writeFuture = writeFuture;
    }

    public Futures.WriteFuture getWriteFuture() {
        return writeFuture;
    }

    public void setWriteFuture(Futures.WriteFuture writeFuture) {
        this.writeFuture = writeFuture;
    }

    public ByteBuffer getMessage() {
        return message;
    }

    public void setMessage(ByteBuffer message) {
        this.message = message;
    }
}
