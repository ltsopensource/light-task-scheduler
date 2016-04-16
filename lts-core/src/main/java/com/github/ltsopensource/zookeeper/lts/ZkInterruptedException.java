package com.github.ltsopensource.zookeeper.lts;

/**
 * @author Robert HG (254963746@qq.com) on 2/18/16.
 */
public class ZkInterruptedException extends ZkException {

    public ZkInterruptedException() {
        super();
    }

    public ZkInterruptedException(String message) {
        super(message);
    }

    public ZkInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkInterruptedException(Throwable cause) {
        super(cause);
    }
}
