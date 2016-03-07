package com.lts.queue.domain;


import com.lts.core.domain.TaskTrackerJobResult;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public class JobFeedbackPo{

    private String id;

    private Long gmtCreated;

    private TaskTrackerJobResult taskTrackerJobResult;

    public TaskTrackerJobResult getTaskTrackerJobResult() {
        return taskTrackerJobResult;
    }

    public void setTaskTrackerJobResult(TaskTrackerJobResult taskTrackerJobResult) {
        this.taskTrackerJobResult = taskTrackerJobResult;
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
