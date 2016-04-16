package com.github.ltsopensource.jobclient.support;

import com.github.ltsopensource.core.exception.JobSubmitException;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobSubmitProtectException extends JobSubmitException {

	private static final long serialVersionUID = -5502779460920973581L;
	int concurrentSize;

    public JobSubmitProtectException(int concurrentSize) {
        super();
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, String message) {
        super(message);
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, String message, Throwable cause) {
        super(message, cause);
        this.concurrentSize = concurrentSize;
    }

    public JobSubmitProtectException(int concurrentSize, Throwable cause) {
        super(cause);
        this.concurrentSize = concurrentSize;
    }
}
