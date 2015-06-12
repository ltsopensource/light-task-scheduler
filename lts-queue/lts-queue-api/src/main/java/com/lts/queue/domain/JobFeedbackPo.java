package com.lts.queue.domain;


import com.lts.core.domain.JobResult;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public class JobFeedbackPo{

    private String id;

    private Long gmtCreated;

    private JobResult jobResult;

    public JobResult getJobResult() {
        return jobResult;
    }

    public void setJobResult(JobResult jobResult) {
        this.jobResult = jobResult;
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
