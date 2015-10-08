package com.lts.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
public class DaoException extends RuntimeException{

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
