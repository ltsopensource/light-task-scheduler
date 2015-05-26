package com.lts.job.client;

import com.lts.job.client.domain.JobClientApplication;
import com.lts.job.client.domain.JobClientNode;
import com.lts.job.client.domain.Response;
import com.lts.job.client.domain.ResponseCode;
import com.lts.job.client.support.JobSubmitProtectException;
import com.lts.job.core.domain.Job;
import com.lts.job.core.support.RetryScheduler;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends JobClient<JobClientNode, JobClientApplication> {

    private RetryScheduler retryScheduler;

    @Override
    protected void innerStart() {

        retryScheduler = new RetryScheduler<Job>(application, 30) {
            @Override
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            @Override
            protected boolean retry(List<Job> jobs) {
                try {
                    return superSubmitJob(jobs).isSuccess();
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
                return false;
            }
        };
        super.innerStart();
        retryScheduler.start();
    }

    @Override
    protected void innerStop() {
        super.innerStop();
        retryScheduler.stop();
    }

    @Override
    public Response submitJob(Job job) {
        return submitJob(Arrays.asList(job));
    }

    @Override
    public Response submitJob(List<Job> jobs) {
        Response response;
        try {
            response = superSubmitJob(jobs);
        } catch (JobSubmitProtectException e) {
            response = new Response();
            response.setSuccess(true);
            response.setFailedJobs(jobs);
            response.setCode(ResponseCode.SUBMIT_TOO_BUSY_AND_SAVE_FOR_LATER);
            response.setMsg(response.getMsg() + ", submit too busy , save local fail store and send later !");
        }
        if (!response.isSuccess()) {
            try {
                for (Job job : response.getFailedJobs()) {
                    retryScheduler.inSchedule(job.getTaskId(), job);
                }
                response.setSuccess(true);
                response.setCode(ResponseCode.SUBMIT_FAILED_AND_SAVE_FOR_LATER);
                response.setMsg(response.getMsg() + ", save local fail store and send later !");
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMsg(e.getMessage());
            }
        }

        return response;
    }

    private Response superSubmitJob(List<Job> jobs) {
        return super.submitJob(jobs);
    }
}
