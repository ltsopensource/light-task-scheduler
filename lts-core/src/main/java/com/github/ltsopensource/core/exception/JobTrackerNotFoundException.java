package com.github.ltsopensource.core.exception;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14.
 * 当没有 找到 JobTracker 节点的时候抛出这个异常
 */
public class JobTrackerNotFoundException extends Exception{

	private static final long serialVersionUID = -7804693020495753429L;

	public JobTrackerNotFoundException() {
    }

    public JobTrackerNotFoundException(String message) {
        super(message);
    }

    public JobTrackerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobTrackerNotFoundException(Throwable cause) {
        super(cause);
    }

}
