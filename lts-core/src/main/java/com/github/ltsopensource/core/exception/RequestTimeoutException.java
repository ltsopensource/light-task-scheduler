package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class RequestTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 7216536669163975612L;

	public RequestTimeoutException() {
        super();
    }

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }

}
