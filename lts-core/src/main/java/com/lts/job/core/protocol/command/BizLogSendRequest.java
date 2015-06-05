package com.lts.job.core.protocol.command;

import com.lts.job.core.constant.Level;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLogSendRequest extends AbstractCommandBody{

    private String jobId;

    private String msg;

    private Level level;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
