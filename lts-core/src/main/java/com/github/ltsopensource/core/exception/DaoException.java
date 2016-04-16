package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
public class DaoException extends RuntimeException{

	private static final long serialVersionUID = -4031278211419963345L;

	public DaoException() {
        super();
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }
}
