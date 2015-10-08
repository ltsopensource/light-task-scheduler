package com.lts.core.domain;

import com.lts.core.commons.utils.JSONUtils;

/**
 * @author Robert HG (254963746@qq.com) on 6/13/15.
 * 发送给客户端的 任务执行结果
 */
public class JobResult {

    private Job job;

    // 执行成功还是失败
    private boolean success;

    private String msg;
    // 任务完成时间
    private Long time;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
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
