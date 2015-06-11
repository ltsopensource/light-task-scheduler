package com.lts.job.biz.logger.domain;

import com.lts.job.core.domain.PageRequest;

import java.util.Date;

/**
 * Created by hugui on 6/11/15.
 */
public class JobLoggerRequest extends PageRequest{

    private String taskId;

    private String taskTrackerNodeGroup;

    private Date startTimestamp;

    private Date endTimestamp;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
}
