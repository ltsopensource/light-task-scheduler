package com.github.ltsopensource.queue.domain;

import com.github.ltsopensource.core.domain.JobType;
import com.github.ltsopensource.core.json.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 *         存储的Jod对象
 */
public class JobPo implements Serializable {

    /**
     * 服务端生成的jobId
     */
    private String jobId;
    /**
     * 任务类型
     */
    private JobType jobType;
    /**
     * 优先级 (数值越大 优先级越低)
     */
    private Integer priority;

    private String taskId;
    /**
     * 真实的taskId
     */
    private String realTaskId;
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
     * 内部使用的扩展参数
     */
    private Map<String, String> internalExtParams;
    /**
     * 是否正在执行
     */
    private Boolean isRunning = false;
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

    private Integer maxRetryTimes = 0;

    /**
     * 重复次数
     */
    private Integer repeatCount = 0;
    /**
     * 已经重复的次数
     */
    private Integer repeatedCount = 0;
    /**
     * 重复interval
     */
    private Long repeatInterval;

    /**
     * 是否依赖上一个执行周期(对于周期性任务才起作用)
     */
    private Boolean relyOnPrevCycle = true;
    // 最后生成的triggerTime
    private Long lastGenerateTriggerTime;

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

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

    public Boolean isRunning() {
        return isRunning;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public void setIsRunning(Boolean isRunning) {
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

    public boolean isCron() {
        return this.cronExpression != null && !"".equals(this.cronExpression.trim());
    }

    public boolean isRepeatable() {
        return (this.repeatInterval != null && this.repeatInterval > 0) && (this.repeatCount >= -1 && this.repeatCount != 0);
    }

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Integer getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(Integer repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public Map<String, String> getInternalExtParams() {
        return internalExtParams;
    }

    public void setInternalExtParams(Map<String, String> internalExtParams) {
        this.internalExtParams = internalExtParams;
    }

    public String getInternalExtParam(String key) {
        if (internalExtParams == null) {
            return null;
        }
        return internalExtParams.get(key);
    }

    public void setInternalExtParam(String key, String value) {
        if (internalExtParams == null) {
            internalExtParams = new HashMap<String, String>();
        }
        internalExtParams.put(key, value);
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Boolean getRelyOnPrevCycle() {
        return relyOnPrevCycle;
    }

    public void setRelyOnPrevCycle(Boolean relyOnPrevCycle) {
        this.relyOnPrevCycle = relyOnPrevCycle;
    }

    public Long getLastGenerateTriggerTime() {
        return lastGenerateTriggerTime;
    }

    public void setLastGenerateTriggerTime(Long lastGenerateTriggerTime) {
        this.lastGenerateTriggerTime = lastGenerateTriggerTime;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
