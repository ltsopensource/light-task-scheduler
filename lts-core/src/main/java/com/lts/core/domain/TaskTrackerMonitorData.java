package com.lts.core.domain;

import com.lts.core.monitor.MonitorData;

/**
 * TaskTracker Monitor Info(MI)
 *
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class TaskTrackerMonitorData extends MonitorData{
    /**
     * 执行成功的个数
     */
    private Long successNum;
    /**
     * 执行失败的个数
     */
    private Long failedNum;
    /**
     * 总的运行时间
     */
    private Long totalRunningTime;
    // FailStore 占用空间
    private Long failStoreSize;

    public Long getFailStoreSize() {
        return failStoreSize;
    }

    public void setFailStoreSize(Long failStoreSize) {
        this.failStoreSize = failStoreSize;
    }

    public Long getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Long successNum) {
        this.successNum = successNum;
    }

    public Long getFailedNum() {
        return failedNum;
    }

    public void setFailedNum(Long failedNum) {
        this.failedNum = failedNum;
    }

    public Long getTotalRunningTime() {
        return totalRunningTime;
    }

    public void setTotalRunningTime(Long totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

}
