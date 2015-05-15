package com.lts.job.core.exception;

/**
 * Created by hugui on 5/12/15.
 */
public class JobSubmitException extends RuntimeException {

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
