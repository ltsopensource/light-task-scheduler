package com.lts.core.protocol.command;


import com.lts.core.domain.Job;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 任务传递信息
 */
public class JobSubmitResponse extends AbstractCommandBody {

    private Boolean success = true;

    private String msg;

    // 失败的jobs
    private List<Job> failedJobs;

    public List<Job> getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(List<Job> failedJobs) {
        this.failedJobs = failedJobs;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
