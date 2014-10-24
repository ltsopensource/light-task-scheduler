package com.lts.job.common.repository.po;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 * 存储的Jod对象
 */
@Entity
public class JobPo {

    /**
     * 服务端生成的jobId
     */
    private String jobId;
    /**
     * 优先级 (数值越大 优先级越低)
     */
    private Integer priority;
    /**
     * 客户端传过来的ID
     */
    private String taskId;
    // 创建时间
    private Long gmtCreate = System.currentTimeMillis();
    // 修改时间
    private Long gmtModify;
    /**
     * 节点组
     */
    private String nodeGroup;
    /**
     * 执行job 的任务节点
     */
    private String taskTrackerNodeGroup;
    /**
     * 额外的参数, 需要传给taskTracker的
     */
    @Embedded(concreteClass = HashMap.class)
    private Map<String, String> extParams;

    /**
     * 备注
     */
    private String remark;
    /**
     * 是否正在执行
     */
    private boolean isRunning = false;
    /**
     * 执行的taskTracker
     * identity
     */
    private String taskTracker;
    /**
     * 是否执行完成
     */
    private boolean isFinished = false;
    // 执行成功还是失败
    private boolean success = false;

    private String msg;

    // 是否需要反馈给客户端
    private boolean needFeedback;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Long getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(Long gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public String getTaskTracker() {
        return taskTracker;
    }

    public void setTaskTracker(String taskTracker) {
        this.taskTracker = taskTracker;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "JobPo{" +
                "jobId='" + jobId + '\'' +
                ", priority=" + priority +
                ", taskId='" + taskId + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModify=" + gmtModify +
                ", nodeGroup='" + nodeGroup + '\'' +
                ", taskTrackerNodeGroup='" + taskTrackerNodeGroup + '\'' +
                ", extParams=" + extParams +
                ", remark='" + remark + '\'' +
                ", isRunning=" + isRunning +
                ", taskTracker='" + taskTracker + '\'' +
                ", isFinished=" + isFinished +
                ", success=" + success +
                ", msg='" + msg + '\'' +
                ", needFeedback=" + needFeedback +
                '}';
    }
}
