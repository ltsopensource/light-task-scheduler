package com.github.ltsopensource.store.jdbc.exception;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class JdbcException extends RuntimeException {

    public JdbcException() {
        super();
    }

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcException(Throwable cause) {
        super(cause);
    }

}
