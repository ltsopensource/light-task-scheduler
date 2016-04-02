package com.lts.tasktracker.runner;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class JobExtInfo {

    // 已经重试的次数 (用户不要设置)
    private int retryTimes = 0;

    /**
     * 已经重复的次数, (用户不要设置)
     */
    private Integer repeatedCount = 0;
    /**
     * 真实的taskId
     */
    private String realTaskId;

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(Integer repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }
}
