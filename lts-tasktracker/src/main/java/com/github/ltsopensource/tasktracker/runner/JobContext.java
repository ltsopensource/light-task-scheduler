package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.domain.Job;

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

}
