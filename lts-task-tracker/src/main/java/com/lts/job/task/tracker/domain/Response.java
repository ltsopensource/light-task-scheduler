package com.lts.job.task.tracker.domain;

import com.lts.job.core.domain.Job;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class Response {

    private boolean success;
    private String msg;

    private Job job;

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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isReceiveNewJob() {
        return receiveNewJob;
    }

    public void setReceiveNewJob(boolean receiveNewJob) {
        this.receiveNewJob = receiveNewJob;
    }
}
