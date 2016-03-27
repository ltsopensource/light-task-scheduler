package com.lts.queue.domain;

import com.lts.core.json.JSON;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJobPo implements Serializable {

    private String depTaskId;

    private String cronExpression;
    /**
     * 重复次数 (-1 表示无限制重复)
     */
    private Integer repeatCount = 0;
    /**
     * 已经重复的次数, (用户不要设置)
     */
    private Integer repeatedCount = 0;

    private Long triggerTime;

    private List<JobEntry> rootJobs;

    private List<JobEntry> leafJobs;

    /**
     * 是否依赖上一周期, 等待上一周期执行完成之后再能提交下一周期
     */
    private Boolean depPrev;
    /**
     * 当前周期ID
     */
    private Integer currentSeqId;

    public String getDepTaskId() {
        return depTaskId;
    }

    public void setDepTaskId(String depTaskId) {
        this.depTaskId = depTaskId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Integer getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(Integer repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public List<JobEntry> getRootJobs() {
        return rootJobs;
    }

    public void setRootJobs(List<JobEntry> rootJobs) {
        this.rootJobs = rootJobs;
    }

    public List<JobEntry> getLeafJobs() {
        return leafJobs;
    }

    public void setLeafJobs(List<JobEntry> leafJobs) {
        this.leafJobs = leafJobs;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Boolean getDepPrev() {
        return depPrev;
    }

    public void setDepPrev(Boolean depPrev) {
        this.depPrev = depPrev;
    }

    public Integer getCurrentSeqId() {
        return currentSeqId;
    }

    public void setCurrentSeqId(Integer currentSeqId) {
        this.currentSeqId = currentSeqId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
