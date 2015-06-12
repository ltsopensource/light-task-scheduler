package com.lts.zookeeper.serializer;

/**
 * @author Robert HG (254963746@qq.com) on 5/17/15.
 */
public class ZkMarshallingException extends RuntimeException {

    public ZkMarshallingException() {
        super();
    }

    public ZkMarshallingException(String message) {
        super(message);
    }

    public ZkMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkMarshallingException(Throwable cause) {
        super(cause);
    }

    protected ZkMarshallingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
