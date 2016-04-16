package com.github.ltsopensource.store.jdbc.exception;

/**
 * @author Robert HG (254963746@qq.com) on 3/8/16.
 */
public class TableNotExistException extends JdbcException {

    public TableNotExistException() {
        super();
    }

    public TableNotExistException(String message) {
        super(message);
    }

    public TableNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableNotExistException(Throwable cause) {
        super(cause);
    }
}
