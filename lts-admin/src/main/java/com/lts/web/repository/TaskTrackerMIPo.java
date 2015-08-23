package com.lts.web.repository;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
public class TaskTrackerMIPo {

    private String id;
    /**
     * 创建时间
     */
    private Long gmtCreated;
    /**
     * TaskTracker NodeGroup
     */
    private String taskTrackerNodeGroup;
    /**
     * TaskTracker 节点标识
     */
    private String taskTrackerIdentity;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public String getTaskTrackerIdentity() {
        return taskTrackerIdentity;
    }

    public void setTaskTrackerIdentity(String taskTrackerIdentity) {
        this.taskTrackerIdentity = taskTrackerIdentity;
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

    public Long getFailStoreSize() {
        return failStoreSize;
    }

    public void setFailStoreSize(Long failStoreSize) {
        this.failStoreSize = failStoreSize;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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
}
