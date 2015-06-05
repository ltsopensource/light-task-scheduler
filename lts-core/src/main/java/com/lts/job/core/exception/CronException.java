package com.lts.job.core.exception;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class CronException extends RuntimeException {

    public CronException() {
        super();
    }

    public CronException(String message) {
        super(message);
    }

    public CronException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronException(Throwable cause) {
        super(cause);
    }

    protected CronException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
