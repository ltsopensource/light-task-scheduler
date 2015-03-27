package com.lts.job.queue.mongo.store;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 */
public class DataStoreException extends RuntimeException {

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStoreException(Throwable cause) {
        super(cause);
    }

    public DataStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
