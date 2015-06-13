package com.lts.tasktracker.domain;

import com.lts.core.domain.JobWrapper;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class Response {

    private boolean success;

    private String msg;

    private JobWrapper jobWrapper;

    /**
     * 是否接收新任务
     */
    private boolean receiveNewJob = true;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public JobWrapper getJobWrapper() {
        return jobWrapper;
    }

    public void setJobWrapper(JobWrapper jobWrapper) {
        this.jobWrapper = jobWrapper;
    }

    public boolean isReceiveNewJob() {
        return receiveNewJob;
    }

    public void setReceiveNewJob(boolean receiveNewJob) {
        this.receiveNewJob = receiveNewJob;
    }
}
