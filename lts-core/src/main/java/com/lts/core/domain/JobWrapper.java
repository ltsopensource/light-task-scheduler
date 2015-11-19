package com.lts.core.domain;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 6/13/15.
 */
public class JobWrapper implements Serializable{

	private static final long serialVersionUID = 1476984243004969158L;

	private String jobId;

    private Job job;

    public JobWrapper(String jobId, Job job) {
        this.jobId = jobId;
        this.job = job;
    }

    public JobWrapper() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "JobWrapper{" +
                "jobId='" + jobId + '\'' +
                ", job=" + job +
                '}';
    }
}
