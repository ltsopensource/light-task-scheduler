package com.lts.job.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 5/12/15.
 */
public class JobSubmitException extends Exception {

    public JobSubmitException() {
        super();
    }

    public JobSubmitException(String message) {
        super(message);
    }

    public JobSubmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobSubmitException(Throwable cause) {
        super(cause);
    }

    protected JobSubmitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
