package com.github.ltsopensource.tasktracker.expcetion;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 * 没有可用的线程
 */
public class NoAvailableJobRunnerException extends Exception{

	private static final long serialVersionUID = 5317008601154858522L;

	public NoAvailableJobRunnerException() {
        super();
    }

    public NoAvailableJobRunnerException(String message) {
        super(message);
    }

    public NoAvailableJobRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableJobRunnerException(Throwable cause) {
        super(cause);
    }

}
