package com.lts.startup;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class CfgException extends Exception {

    public CfgException() {
        super();
    }

    public CfgException(String message) {
        super(message);
    }

    public CfgException(String message, Throwable cause) {
        super(message, cause);
    }

    public CfgException(Throwable cause) {
        super(cause);
    }
}
