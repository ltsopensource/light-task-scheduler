package com.github.ltsopensource.core.protocol.command;


import com.github.ltsopensource.core.domain.Job;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 任务传递信息
 */
public class JobSubmitResponse extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 9133108871954698698L;

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
