package com.lts.core.domain;

import java.util.List;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJobGroup {

    private List<Job> jobs;

    private String groupId;

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

    /**
     * 是否依赖上一周期, 等待上一周期执行完成之后再能提交下一周期
     */
    private Boolean depPrev;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
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

    public Boolean getDepPrev() {
        return depPrev;
    }

    public void setDepPrev(Boolean depPrev) {
        this.depPrev = depPrev;
    }
}
