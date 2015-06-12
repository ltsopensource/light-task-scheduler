package com.lts.store.jdbc;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public class StateException extends RuntimeException {

    public StateException() {
    }

    public StateException(String message) {
        super(message);
    }

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateException(Throwable cause) {
        super(cause);
    }
}
