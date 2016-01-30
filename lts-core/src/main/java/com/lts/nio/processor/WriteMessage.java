package com.lts.nio.processor;

import com.lts.nio.handler.WriteFuture;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class WriteMessage {

    private ByteBuffer message;
    private WriteFuture writeFuture;

    public WriteMessage(ByteBuffer message, WriteFuture writeFuture) {
        this.message = message;
        this.writeFuture = writeFuture;
    }

    public WriteFuture getWriteFuture() {
        return writeFuture;
    }

    public void setWriteFuture(WriteFuture writeFuture) {
        this.writeFuture = writeFuture;
    }

    public ByteBuffer getMessage() {
        return message;
    }

    public void setMessage(ByteBuffer message) {
        this.message = message;
    }
}
