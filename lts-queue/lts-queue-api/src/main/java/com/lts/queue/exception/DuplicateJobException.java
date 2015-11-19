package com.lts.queue.exception;

/**
 * 向任务队列中添加任务的时候，会出现任务重复的情况，就会抛出这个异常
 * @author Robert HG (254963746@qq.com) on 3/26/15.
 */
public class DuplicateJobException extends RuntimeException {

	private static final long serialVersionUID = 2049995998886965479L;

	public DuplicateJobException() {
        super();
    }

    public DuplicateJobException(String message) {
        super(message);
    }

    public DuplicateJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateJobException(Throwable cause) {
        super(cause);
    }

}
