package com.lts.web.repository;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
public class TaskTrackerMonitorDataPo extends AbstractMonitorDataPo {

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

}
