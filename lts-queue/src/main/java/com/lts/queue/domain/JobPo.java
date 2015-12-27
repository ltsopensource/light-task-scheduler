package com.lts.queue.domain;

import com.lts.core.json.JSON;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         存储的Jod对象
 */
public class JobPo {

    /**
     * 服务端生成的jobId
     */
    private String jobId;
    /**
     * 优先级 (数值越大 优先级越低)
     */
    private Integer priority;
    /**
     * 客户端传过来的ID
     */
    private String taskId;
    // 创建时间
    private Long gmtCreated;
    // 修改时间
    private Long gmtModified;
    /**
     * 提交客户端的节点组
     */
    private String submitNodeGroup;
    /**
     * 执行job 的任务节点
     */
    private String taskTrackerNodeGroup;
    /**
     * 额外的参数, 需要传给taskTracker的
     */
    private Map<String, String> extParams;
    /**
     * 是否正在执行
     */
    private boolean isRunning = false;
    /**
     * 执行的taskTracker
     * identity
     */
    private String taskTrackerIdentity;

    // 是否需要反馈给客户端
    private boolean needFeedback;

    /**
     * 执行时间表达式 (和 quartz 表达式一样)
     */
    private String cronExpression;
    /**
     * 下一次执行时间
     */
    private Long triggerTime;

    /**
     * 重试次数
     */
    private Integer retryTimes = 0;

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

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

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public String getSubmitNodeGroup() {
        return submitNodeGroup;
    }

    public void setSubmitNodeGroup(String submitNodeGroup) {
        this.submitNodeGroup = submitNodeGroup;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public boolean isSchedule() {
        return this.cronExpression != null && !"".equals(this.cronExpression.trim());
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
