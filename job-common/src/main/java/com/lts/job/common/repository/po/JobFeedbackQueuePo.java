package com.lts.job.common.repository.po;

import com.lts.job.common.domain.JobResult;

/**
 * Created by hugui on 3/3/15.
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
