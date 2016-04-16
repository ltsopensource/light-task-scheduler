package com.github.ltsopensource.biz.logger.domain;

import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.domain.JobType;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/20/14.
 *         任务执行 日志
 */
public class JobLogPo {

    // 日志记录时间
    private Long logTime;
    // 日志记录时间
    private Long gmtCreated;
    private JobType jobType;
    // 日志类型
    private LogType logType;
    private boolean success;
    private String msg;
    private String taskTrackerIdentity;

    // 日志记录级别
    private Level level;

    private String jobId;
    private String taskId;
    private String realTaskId;
    /**
     * 优先级 (数值越大 优先级越低)
     */
    private Integer priority = 100;
    // 提交的节点
    private String submitNodeGroup;
    // 执行的节点
    private String taskTrackerNodeGroup;

    private Map<String, String> extParams;
    /**
     * 内部使用的扩展参数
     */
    private Map<String, String> internalExtParams;
    // 是否要反馈给客户端
    private boolean needFeedback = true;
    /**
     * 执行表达式 和 quartz 的一样
     * 如果这个为空，表示立即执行的
     */
    private String cronExpression;
    /**
     * 任务的最早出发时间
     */
    private Long triggerTime;

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

    private Boolean depPreCycle;

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public Map<String, String> getInternalExtParams() {
        return internalExtParams;
    }

    public void setInternalExtParams(Map<String, String> internalExtParams) {
        this.internalExtParams = internalExtParams;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
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

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
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

    public Integer getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(Integer repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }

    public Boolean getDepPreCycle() {
        return depPreCycle;
    }

    public void setDepPreCycle(Boolean depPreCycle) {
        this.depPreCycle = depPreCycle;
    }
}
