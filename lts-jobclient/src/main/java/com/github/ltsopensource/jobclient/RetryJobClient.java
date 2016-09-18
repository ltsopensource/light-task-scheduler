package com.github.ltsopensource.jobclient;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.failstore.FailStorePathBuilder;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.support.RetryScheduler;
import com.github.ltsopensource.jobclient.domain.JobClientAppContext;
import com.github.ltsopensource.jobclient.domain.JobClientNode;
import com.github.ltsopensource.jobclient.domain.Response;
import com.github.ltsopensource.jobclient.domain.ResponseCode;
import com.github.ltsopensource.jobclient.support.JobSubmitProtectException;

import java.util.Collections;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends JobClient<JobClientNode, JobClientAppContext> {

    private RetryScheduler<Job> jobRetryScheduler;

    @Override
    protected void beforeStart() {
        super.beforeStart();
        jobRetryScheduler = new RetryScheduler<Job>(RetryJobClient.class.getSimpleName(), appContext,
                FailStorePathBuilder.getJobSubmitFailStorePath(appContext), 10) {
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            protected boolean retry(List<Job> jobs) {
                Response response = null;
                try {
                    // 重试必须走同步，不然会造成文件锁，死锁
                    response = superSubmitJob(jobs, SubmitType.SYNC);
                    return response.isSuccess();
                } catch (Throwable t) {
                    RetryScheduler.LOGGER.error(t.getMessage(), t);
                } finally {
                    if (response != null && response.isSuccess()) {
                        stat.incSubmitFailStoreNum(jobs.size());
                    }
                }
                return false;
            }
        };
        jobRetryScheduler.start();
    }

    @Override
    protected void beforeStop() {
        super.beforeStop();
        jobRetryScheduler.stop();
    }

    @Override
    public Response submitJob(Job job) {
        return submitJob(Collections.singletonList(job));
    }

    @Override
    public Response submitJob(List<Job> jobs) {

        Response response;
        try {
            response = superSubmitJob(jobs);
        } catch (JobSubmitProtectException e) {
            response = new Response();
            response.setSuccess(false);
            response.setFailedJobs(jobs);
            response.setCode(ResponseCode.SUBMIT_TOO_BUSY_AND_SAVE_FOR_LATER);
            response.setMsg(response.getMsg() + ", submit too busy");
        }
        if (!response.isSuccess()) {
            try {
                for (Job job : response.getFailedJobs()) {
                    jobRetryScheduler.inSchedule(job.getTaskId(), job);
                    stat.incFailStoreNum();
                }
                response.setSuccess(true);
                response.setCode(ResponseCode.SUBMIT_FAILED_AND_SAVE_FOR_LATER);
                response.setMsg(response.getMsg() + ", save local fail store and send later !");
                LOGGER.warn(JSON.toJSONString(response));
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
