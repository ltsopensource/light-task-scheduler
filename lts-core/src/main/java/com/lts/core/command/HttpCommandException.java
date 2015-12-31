package com.lts.core.command;

/**
 * @author Robert HG (254963746@qq.com) on 10/28/15.
 */
public class HttpCommandException extends RuntimeException{

	private static final long serialVersionUID = 7563802613921477340L;

	public HttpCommandException() {
        super();
    }

    public HttpCommandException(String message) {
        super(message);
    }

    public HttpCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpCommandException(Throwable cause) {
        super(cause);
    }
}
