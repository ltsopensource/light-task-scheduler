package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class RemotingSendException extends Exception{

	private static final long serialVersionUID = -8901776781734789960L;

	public RemotingSendException() {
        super();
    }

    public RemotingSendException(String message) {
        super(message);
    }

    public RemotingSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemotingSendException(Throwable cause) {
        super(cause);
    }

}
