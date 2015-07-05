package com.lts.jobclient.support;

import com.lts.core.constant.Constants;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;
import com.lts.jobclient.domain.Response;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 用来处理客户端请求过载问题
 *
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobSubmitProtector {

    private int concurrentSize = Constants.AVAILABLE_PROCESSOR * 4;
    // 用信号量进行过载保护
    private Semaphore semaphore;
    private int timeout = 500;
    private String errorMsg;

    public JobSubmitProtector() {
        errorMsg = "the concurrent size is " + concurrentSize +
                " , submit too fast , use " + Constants.JOB_SUBMIT_CONCURRENCY_SIZE +
                " can change the concurrent size .";
    }

    public JobSubmitProtector(int concurrentSize) {
        this();
        if (concurrentSize > 0) {
            this.concurrentSize = concurrentSize;
        }
        semaphore = new Semaphore(this.concurrentSize);
    }

    public Response execute(final List<Job> jobs, final JobSubmitExecutor<Response> jobSubmitExecutor) throws JobSubmitException {
        boolean acquire = false;
        try {
            try {
                acquire = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
                if (!acquire) {
                    throw new JobSubmitProtectException(concurrentSize, errorMsg);
                }
            } catch (InterruptedException e) {
                throw new JobSubmitProtectException(concurrentSize, errorMsg);
            }
            return jobSubmitExecutor.execute(jobs);
        } finally {
            if (acquire) {
                semaphore.release();
            }
        }
    }

    public void getConcurrentSize(int concurrentSize) {
        this.concurrentSize = concurrentSize;
    }
}
