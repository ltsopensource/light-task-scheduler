package com.lts.job.core.repository.po;

import com.lts.job.core.domain.JobResult;

/**
 * Created by Robert HG (254963746@qq.com) on 3/3/15.
 */
public class JobFeedbackQueuePo extends JobResult{

    private String id;

    private Long gmtCreated;

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
