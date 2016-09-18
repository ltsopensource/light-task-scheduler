package com.github.ltsopensource.kv;

/**
 * @author Robert HG (254963746@qq.com) on 12/13/15.
 */
public class DBException extends RuntimeException {

    public DBException() {
        super();
    }

    public DBException(String s) {
        super(s);
    }

    public DBException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DBException(Throwable throwable) {
        super(throwable);
    }
}
