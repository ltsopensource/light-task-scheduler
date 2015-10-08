package com.lts.web.request;

import java.util.Date;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public class JobQueueRequest extends PageRequest {

    // ------------ 下面是查询条件值 ---------------
    private String jobId;

    private String taskId;

    private String submitNodeGroup;

    private String taskTrackerNodeGroup;

    private Date startGmtCreated;
    private Date endGmtCreated;
    private Date startGmtModified;
    private Date endGmtModified;

    // ------------ 下面是能update的值 -------------------

    private String cronExpression;

    private Boolean needFeedback;

    private Map<String, String> extParams;

    private Date triggerTime;

    private Integer priority;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubmitNodeGroup() {
        return submitNodeGroup;
    }

    public void setSubmitNodeGroup(String submitNodeGroup) {
        this.submitNodeGroup = submitNodeGroup;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(Boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Date getStartGmtCreated() {
        return startGmtCreated;
    }

    public void setStartGmtCreated(Date startGmtCreated) {
        this.startGmtCreated = startGmtCreated;
    }

    public Date getEndGmtCreated() {
        return endGmtCreated;
    }

    public void setEndGmtCreated(Date endGmtCreated) {
        this.endGmtCreated = endGmtCreated;
    }

    public Date getStartGmtModified() {
        return startGmtModified;
    }

    public void setStartGmtModified(Date startGmtModified) {
        this.startGmtModified = startGmtModified;
    }

    public Date getEndGmtModified() {
        return endGmtModified;
    }

    public void setEndGmtModified(Date endGmtModified) {
        this.endGmtModified = endGmtModified;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }
}
