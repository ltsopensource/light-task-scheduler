package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 3/2/16.
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
