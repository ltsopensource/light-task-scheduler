package com.github.ltsopensource.spring.tasktracker;

/**
 * @author Robert HG (254963746@qq.com) on 10/21/15.
 */
public class JobDispatchException extends Exception{

	private static final long serialVersionUID = -99670791735250890L;

	public JobDispatchException() {
        super();
    }

    public JobDispatchException(String message) {
        super(message);
    }

    public JobDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobDispatchException(Throwable cause) {
        super(cause);
    }
}
