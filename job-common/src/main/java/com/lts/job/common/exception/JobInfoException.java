package com.lts.job.common.exception;

/**
 * 这个异常不会打印堆栈信息
 * Created by hugui on 1/8/15.
 */
public class JobInfoException extends Exception {

    public JobInfoException() {
        super();
    }

    public JobInfoException(String message) {
        super(message);
    }

    public JobInfoException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobInfoException(Throwable cause) {
        super(cause);
    }

    protected JobInfoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
