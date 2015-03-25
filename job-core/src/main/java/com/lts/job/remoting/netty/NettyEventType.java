package com.lts.job.remoting.netty;

/**
 * Netty产生的事件类型
 */
public enum NettyEventType {
    CONNECT,
    CLOSE,
    READER_IDLE,
    WRITER_IDLE,
    ALL_IDLE,
    EXCEPTION
}
