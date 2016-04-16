package com.github.ltsopensource.startup.jobtracker;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class CfgException extends Exception {

	private static final long serialVersionUID = -661377294271386745L;

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
