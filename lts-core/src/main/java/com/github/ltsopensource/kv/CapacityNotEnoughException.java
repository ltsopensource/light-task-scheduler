package com.github.ltsopensource.kv;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class CapacityNotEnoughException extends DBException{

    public CapacityNotEnoughException() {
    }

    public CapacityNotEnoughException(String s) {
        super(s);
    }

    public CapacityNotEnoughException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CapacityNotEnoughException(Throwable throwable) {
        super(throwable);
    }
}
