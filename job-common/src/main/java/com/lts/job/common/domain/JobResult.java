package com.lts.job.common.domain;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 * 任务执行结果
 */
public class JobResult {

    private Job job;

    // 执行成功还是失败
    private boolean success;

    private String msg;

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

    @Override
    public String toString() {
        return "JobResult{" +
                "job=" + job +
                ", success=" + success +
                ", msg='" + msg + '\'' +
                '}';
    }
}
