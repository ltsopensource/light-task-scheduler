package com.lts.jobclient.support;

import com.lts.core.commons.concurrent.limiter.RateLimiter;
import com.lts.core.constant.Constants;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;
import com.lts.jobclient.domain.JobClientAppContext;
import com.lts.jobclient.domain.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用来处理客户端请求过载问题
 *
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public class JobSubmitProtector {

    private int maxQPS;
    // 用信号量进行过载保护
    RateLimiter rateLimiter;
    private int acquireTimeout = 100;
    private String errorMsg;

    public JobSubmitProtector(JobClientAppContext appContext) {

        this.maxQPS = appContext.getConfig().getParameter(Constants.JOB_SUBMIT_MAX_QPS,
                Constants.DEFAULT_JOB_SUBMIT_MAX_QPS);
        if (this.maxQPS < 10) {
            this.maxQPS = Constants.DEFAULT_JOB_SUBMIT_MAX_QPS;
        }

        this.errorMsg = "the maxQPS is " + maxQPS +
                " , submit too fast , use " + Constants.JOB_SUBMIT_MAX_QPS +
                " can change the concurrent size .";
        this.acquireTimeout = appContext.getConfig().getParameter("job.submit.lock.acquire.timeout", 100);

        this.rateLimiter = RateLimiter.create(this.maxQPS);
    }

    public Response execute(final List<Job> jobs, final JobSubmitExecutor<Response> jobSubmitExecutor) throws JobSubmitException {
        if (!rateLimiter.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS)) {
            throw new JobSubmitProtectException(maxQPS, errorMsg);
        }
        return jobSubmitExecutor.execute(jobs);
    }

    public int getMaxQPS() {
        return maxQPS;
    }
}
