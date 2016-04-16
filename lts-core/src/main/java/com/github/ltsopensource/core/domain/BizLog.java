package com.github.ltsopensource.core.domain;

import com.github.ltsopensource.core.constant.Level;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 6/12/15.
 */
public class BizLog implements Serializable {

	private static final long serialVersionUID = -7770486329649514754L;

	private String taskId;
    private JobType jobType;
    private String jobId;

    private String realTaskId;

    private String msg;

    private Level level;

    private Long logTime;

    private String taskTrackerIdentity;

    private String taskTrackerNodeGroup;

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }
}
