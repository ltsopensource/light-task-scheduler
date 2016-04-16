package com.github.ltsopensource.admin.request;

/**
 * @author Robert HG (254963746@qq.com) on 9/28/15.
 */
public class JvmDataReq {

    private String identity;

    private Long startTime;

    private Long endTime;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
