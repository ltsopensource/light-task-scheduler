package com.lts.core.domain;

/**
 * TaskTracker Monitor Info(MI)
 *
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class TaskTrackerMI {
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
    private Long timestamp;
    // 最大内存
    private Long maxMemory;
    // 已分配内存
    private Long allocatedMemory;
    // 已分配内存中的剩余内存
    private Long freeMemory;
    // 总的空闲内存
    private Long totalFreeMemory;

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

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Long getAllocatedMemory() {
        return allocatedMemory;
    }

    public void setAllocatedMemory(Long allocatedMemory) {
        this.allocatedMemory = allocatedMemory;
    }

    public Long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(Long freeMemory) {
        this.freeMemory = freeMemory;
    }

    public Long getTotalFreeMemory() {
        return totalFreeMemory;
    }

    public void setTotalFreeMemory(Long totalFreeMemory) {
        this.totalFreeMemory = totalFreeMemory;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
