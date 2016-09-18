package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.tasktracker.logger.BizLogger;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class JobContext {

    /**
     * 用户提交的job
     */
    private Job job;
    /**
     * 额外的一些信息
     */
    private JobExtInfo jobExtInfo;

    private BizLogger bizLogger;

    public BizLogger getBizLogger() {
        return bizLogger;
    }

    public void setBizLogger(BizLogger bizLogger) {
        this.bizLogger = bizLogger;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public JobExtInfo getJobExtInfo() {
        return jobExtInfo;
    }

    public void setJobExtInfo(JobExtInfo jobExtInfo) {
        this.jobExtInfo = jobExtInfo;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
