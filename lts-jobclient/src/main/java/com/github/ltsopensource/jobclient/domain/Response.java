package com.github.ltsopensource.jobclient.domain;


import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.json.JSON;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 * 返回给客户端的
 */
public class Response implements Serializable {

	private static final long serialVersionUID = 1466250084442936848L;
	private boolean success;
    private String msg;
    private String code;

    // 如果success 为false, 这个才会有值
    private List<Job> failedJobs;

    public List<Job> getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(List<Job> failedJobs) {
        this.failedJobs = failedJobs;
    }

    public void addFailedJobs(List<Job> jobs){
        if(this.failedJobs == null){
            this.failedJobs = new ArrayList<Job>();
        }
        this.failedJobs.addAll(jobs);
    }

    public void addFailedJob(Job job){
        if(this.failedJobs == null){
            this.failedJobs = new ArrayList<Job>();
        }
        this.failedJobs.add(job);
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
