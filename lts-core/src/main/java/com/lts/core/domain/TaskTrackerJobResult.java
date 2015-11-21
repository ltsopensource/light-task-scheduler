package com.lts.core.domain;

import com.lts.core.json.JSON;

import java.io.Serializable;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 * TaskTracker 任务执行结果
 */
public class TaskTrackerJobResult implements Serializable{

	private static final long serialVersionUID = 8622758290605000897L;

	private JobWrapper jobWrapper;

    private Action action;

    private String msg;
    // 任务完成时间
    private Long time;

    public JobWrapper getJobWrapper() {
        return jobWrapper;
    }

    public void setJobWrapper(JobWrapper jobWrapper) {
        this.jobWrapper = jobWrapper;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
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
        return JSON.toJSONString(this);
    }
}
