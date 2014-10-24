package com.lts.job.common.repository.po;

import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.LogType;

/**
 * @author Robert HG (254963746@qq.com) on 8/20/14.
 * 任务执行 日志
 */
public class JobLogPo extends Job {

    // 日志记录时间
    private Long timestamp = System.currentTimeMillis();
    private LogType logType;
    private boolean success;
    private String msg;
    private String code;

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
