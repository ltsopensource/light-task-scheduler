package com.github.ltsopensource.biz.logger;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobLogException extends RuntimeException {

	private static final long serialVersionUID = -7907389604614654285L;

	public JobLogException() {
        super();
    }

    public JobLogException(String message) {
        super(message);
    }

    public JobLogException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobLogException(Throwable cause) {
        super(cause);
    }

}
