package com.github.ltsopensource.queue.domain;


import com.github.ltsopensource.core.domain.JobRunResult;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public class JobFeedbackPo{

    private String id;

    private Long gmtCreated;

    private JobRunResult jobRunResult;

    public JobRunResult getJobRunResult() {
        return jobRunResult;
    }

    public void setJobRunResult(JobRunResult jobRunResult) {
        this.jobRunResult = jobRunResult;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
