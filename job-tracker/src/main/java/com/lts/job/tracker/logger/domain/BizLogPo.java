package com.lts.job.tracker.logger.domain;

import com.lts.job.core.constant.Level;

/**
 * Created by hugui on 3/30/15.
 */
public class BizLogPo {

    private Long timestamp;

    private String taskTrackerNodeGroup;

    /**
     * 当前节点的唯一标识
     */
    private String taskTrackerIdentity;

    /**
     * 任务ID
     */
    private String jobId;
    /**
     * 日志信息
     */
    private String msg;

    /**
     * 日志级别
     */
    private Level level;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }
}
