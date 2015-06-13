package com.lts.core.domain;

import com.lts.core.commons.utils.JSONUtils;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 * TaskTracker 任务执行结果
 */
public class TaskTrackerJobResult {

    private JobWrapper jobWrapper;

    // 执行成功还是失败
    private boolean success;

    private String msg;
    // 任务完成时间
    private Long time;

    public JobWrapper getJobWrapper() {
        return jobWrapper;
    }

    public void setJobWrapper(JobWrapper jobWrapper) {
        this.jobWrapper = jobWrapper;
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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }
}
