package com.lts.job.tracker.logger.domain;

import com.lts.job.core.constant.Level;
import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.LogType;

/**
 * @author Robert HG (254963746@qq.com) on 8/20/14.
 * 任务执行 日志
 */
public class JobLogPo extends Job{

    // 日志记录时间
    private Long timestamp = System.currentTimeMillis();
    // 日志类型
    private LogType logType;
    private boolean success;
    private String msg;
    private String code;
    private String taskTrackerIdentity;

    // 日志记录级别
    private Level level;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
