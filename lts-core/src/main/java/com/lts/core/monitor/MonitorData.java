package com.lts.core.monitor;

/**
 * @author Robert HG (254963746@qq.com) on 8/30/15.
 */
public class MonitorData {

    private Long timestamp;
    // 最大内存
    private Long maxMemory;
    // 已分配内存
    private Long allocatedMemory;
    // 已分配内存中的剩余内存
    private Long freeMemory;
    // 总的空闲内存
    private Long totalFreeMemory;

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
