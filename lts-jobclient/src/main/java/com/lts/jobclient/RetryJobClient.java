package com.lts.jobclient;

import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.domain.Job;
import com.lts.core.support.RetryScheduler;
import com.lts.jobclient.domain.JobClientApplication;
import com.lts.jobclient.domain.JobClientNode;
import com.lts.jobclient.domain.Response;
import com.lts.jobclient.domain.ResponseCode;
import com.lts.jobclient.support.JobSubmitProtectException;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends JobClient<JobClientNode, JobClientApplication> {

    private RetryScheduler retryScheduler;

    @Override
    protected void preRemotingStart() {

        retryScheduler = new RetryScheduler<Job>(application, 30) {
            @Override
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            @Override
            protected boolean retry(List<Job> jobs) {
                try {
                    // 重试必须走同步，不然会造成文件锁，死锁
                    return superSubmitJob(jobs, SubmitType.SYNC).isSuccess();
                } catch (Throwable t) {
                    RetryScheduler.LOGGER.error(t.getMessage(), t);
                }
                return false;
            }
        };
        retryScheduler.setName(RetryJobClient.class.getSimpleName());
        super.preRemotingStart();
        retryScheduler.start();
    }

    @Override
    protected void preRemotingStop() {
        super.preRemotingStop();
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
            LOGGER.warn(JSONUtils.toJSONString(response));
            return response;
        }
        if (!response.isSuccess()) {
            try {
                for (Job job : response.getFailedJobs()) {
                    retryScheduler.inSchedule(job.getTaskId(), job);
                }
                response.setSuccess(true);
                response.setCode(ResponseCode.SUBMIT_FAILED_AND_SAVE_FOR_LATER);
                response.setMsg(response.getMsg() + ", save local fail store and send later !");
                LOGGER.warn(JSONUtils.toJSONString(response));
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

    private Response superSubmitJob(List<Job> jobs, SubmitType type) {
        return super.submitJob(jobs, type);
    }
}
