package com.lts.core.failstore;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
public class FailStoreException extends Exception {

    public FailStoreException(String message) {
        super(message);
    }

    public FailStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailStoreException(Throwable cause) {
        super(cause);
    }

    protected FailStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
