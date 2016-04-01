package com.lts.jobclient;

import com.lts.core.domain.DepJobGroup;
import com.lts.core.domain.Job;
import com.lts.core.failstore.FailStorePathBuilder;
import com.lts.core.json.JSON;
import com.lts.core.support.RetryScheduler;
import com.lts.jobclient.domain.JobClientAppContext;
import com.lts.jobclient.domain.JobClientNode;
import com.lts.jobclient.domain.Response;
import com.lts.jobclient.domain.ResponseCode;
import com.lts.jobclient.support.JobSubmitProtectException;

import java.util.Collections;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends JobClient<JobClientNode, JobClientAppContext> {

    private RetryScheduler<Job> jobRetryScheduler;
    private RetryScheduler<DepJobGroup> depJobRetryScheduler;

    @Override
    protected void beforeStart() {
        super.beforeStart();
        jobRetryScheduler = new RetryScheduler<Job>(appContext,
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
        jobRetryScheduler.setName(RetryJobClient.class.getSimpleName());
        jobRetryScheduler.start();

        depJobRetryScheduler = new RetryScheduler<DepJobGroup>(appContext,
                FailStorePathBuilder.getDepJobSubmitFailStorePath(appContext), 1) {
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            protected boolean retry(List<DepJobGroup> list) {
                Response response = null;
                try {
                    DepJobGroup jobGroup = list.get(0);
                    // 重试必须走同步，不然会造成文件锁，死锁
                    response = superSubmitJob(jobGroup, SubmitType.SYNC);
                    return response.isSuccess();
                } catch (Throwable t) {
                    RetryScheduler.LOGGER.error(t.getMessage(), t);
                } finally {
                    if (response != null && response.isSuccess()) {
                        stat.incSubmitFailStoreNum(1);
                    }
                }
                return false;
            }
        };
        depJobRetryScheduler.setName("DepJobGroup_RetryJobClient");
        depJobRetryScheduler.start();
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

    public Response submitJob(DepJobGroup jobGroup) {
        Response response;
        try {
            response = super.submitJob(jobGroup);
        } catch (JobSubmitProtectException e) {
            response = new Response();
            response.setSuccess(false);
            response.setCode(ResponseCode.SUBMIT_TOO_BUSY_AND_SAVE_FOR_LATER);
            response.setMsg(response.getMsg() + ", submit too busy");
        }
        if (!response.isSuccess()) {
            try {
                depJobRetryScheduler.inSchedule(jobGroup.getGroupId(), jobGroup);
                stat.incFailStoreNum();
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

    private Response superSubmitJob(DepJobGroup jobGroup, SubmitType type) {
        return super.submitJob(jobGroup, type);
    }
}
