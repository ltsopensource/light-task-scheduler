package com.github.ltsopensource.monitor.access.domain;

/**
 * @author Robert HG (254963746@qq.com) on 9/27/15.
 */
public class JVMGCDataPo extends MDataPo {

    private Long youngGCCollectionCount;
    private Long youngGCCollectionTime;
    private Long fullGCCollectionCount;
    private Long fullGCCollectionTime;
    private Long spanYoungGCCollectionCount;
    private Long spanYoungGCCollectionTime;
    private Long spanFullGCCollectionCount;
    private Long spanFullGCCollectionTime;

    public Long getYoungGCCollectionCount() {
        return youngGCCollectionCount;
    }

    public void setYoungGCCollectionCount(Long youngGCCollectionCount) {
        this.youngGCCollectionCount = youngGCCollectionCount;
    }

    public Long getYoungGCCollectionTime() {
        return youngGCCollectionTime;
    }

    public void setYoungGCCollectionTime(Long youngGCCollectionTime) {
        this.youngGCCollectionTime = youngGCCollectionTime;
    }

    public Long getFullGCCollectionCount() {
        return fullGCCollectionCount;
    }

    public void setFullGCCollectionCount(Long fullGCCollectionCount) {
        this.fullGCCollectionCount = fullGCCollectionCount;
    }

    public Long getFullGCCollectionTime() {
        return fullGCCollectionTime;
    }

    public void setFullGCCollectionTime(Long fullGCCollectionTime) {
        this.fullGCCollectionTime = fullGCCollectionTime;
    }

    public Long getSpanYoungGCCollectionCount() {
        return spanYoungGCCollectionCount;
    }

    public void setSpanYoungGCCollectionCount(Long spanYoungGCCollectionCount) {
        this.spanYoungGCCollectionCount = spanYoungGCCollectionCount;
    }

    public Long getSpanYoungGCCollectionTime() {
        return spanYoungGCCollectionTime;
    }

    public void setSpanYoungGCCollectionTime(Long spanYoungGCCollectionTime) {
        this.spanYoungGCCollectionTime = spanYoungGCCollectionTime;
    }

    public Long getSpanFullGCCollectionCount() {
        return spanFullGCCollectionCount;
    }

    public void setSpanFullGCCollectionCount(Long spanFullGCCollectionCount) {
        this.spanFullGCCollectionCount = spanFullGCCollectionCount;
    }

    public Long getSpanFullGCCollectionTime() {
        return spanFullGCCollectionTime;
    }

    public void setSpanFullGCCollectionTime(Long spanFullGCCollectionTime) {
        this.spanFullGCCollectionTime = spanFullGCCollectionTime;
    }
}
