package com.lts.job.biz.logger;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobLogException extends RuntimeException {

    public JobLogException() {
        super();
    }

    public JobLogException(String message) {
        super(message);
    }

    public JobLogException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobLogException(Throwable cause) {
        super(cause);
    }

    protected JobLogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
