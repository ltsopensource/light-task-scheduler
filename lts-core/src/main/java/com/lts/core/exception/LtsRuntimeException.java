package com.lts.core.exception;

/**
 * Created by hugui.hg on 3/2/16.
 */
public class LtsRuntimeException extends RuntimeException {

    public LtsRuntimeException() {
        super();
    }

    public LtsRuntimeException(String message) {
        super(message);
    }

    public LtsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LtsRuntimeException(Throwable cause) {
        super(cause);
    }
}
