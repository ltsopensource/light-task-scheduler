package com.lts.job.core.exception;

/**
 * 这个异常不会打印堆栈信息
 * @author Robert HG (254963746@qq.com) on 1/8/15.
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
