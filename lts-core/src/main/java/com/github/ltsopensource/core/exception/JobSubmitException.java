package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 5/12/15.
 */
public class JobSubmitException extends RuntimeException {

	private static final long serialVersionUID = 8375498515729588730L;

	public JobSubmitException() {
        super();
    }

    public JobSubmitException(String message) {
        super(message);
    }

    public JobSubmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobSubmitException(Throwable cause) {
        super(cause);
    }

}
