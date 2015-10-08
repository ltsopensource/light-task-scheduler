package com.lts.tasktracker.domain;

import com.lts.core.domain.Action;
import com.lts.core.domain.JobWrapper;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class Response {

    private Action action;

    private String msg;

    private JobWrapper jobWrapper;

    /**
     * 是否接收新任务
     */
    private boolean receiveNewJob = true;

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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
