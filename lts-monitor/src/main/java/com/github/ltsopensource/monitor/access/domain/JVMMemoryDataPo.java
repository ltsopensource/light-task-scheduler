package com.github.ltsopensource.monitor.access.domain;

/**
 * @author Robert HG (254963746@qq.com) on 9/27/15.
 */
public class JVMMemoryDataPo extends MDataPo {

    // Heap
    private Long heapMemoryCommitted;

    private Long heapMemoryInit;

    private Long heapMemoryMax;

    private Long heapMemoryUsed;

    // NonHeap
    private Long nonHeapMemoryCommitted;

    private Long nonHeapMemoryInit;

    private Long nonHeapMemoryMax;

    private Long nonHeapMemoryUsed;

    // PermGen
    private Long permGenCommitted;

    private Long permGenInit;

    private Long permGenMax;

    private Long permGenUsed;

    // OldGen
    private Long oldGenCommitted;

    private Long oldGenInit;

    private Long oldGenMax;

    private Long oldGenUsed;

    // EdenSpace
    private Long edenSpaceCommitted;

    private Long edenSpaceInit;

    private Long edenSpaceMax;

    private Long edenSpaceUsed;

    // Survivor
    private Long survivorCommitted;

    private Long survivorInit;

    private Long survivorMax;

    private Long survivorUsed;

    public Long getHeapMemoryCommitted() {
        return heapMemoryCommitted;
    }

    public void setHeapMemoryCommitted(Long heapMemoryCommitted) {
        this.heapMemoryCommitted = heapMemoryCommitted;
    }

    public Long getHeapMemoryInit() {
        return heapMemoryInit;
    }

    public void setHeapMemoryInit(Long heapMemoryInit) {
        this.heapMemoryInit = heapMemoryInit;
    }

    public Long getHeapMemoryMax() {
        return heapMemoryMax;
    }

    public void setHeapMemoryMax(Long heapMemoryMax) {
        this.heapMemoryMax = heapMemoryMax;
    }

    public Long getHeapMemoryUsed() {
        return heapMemoryUsed;
    }

    public void setHeapMemoryUsed(Long heapMemoryUsed) {
        this.heapMemoryUsed = heapMemoryUsed;
    }

    public Long getNonHeapMemoryCommitted() {
        return nonHeapMemoryCommitted;
    }

    public void setNonHeapMemoryCommitted(Long nonHeapMemoryCommitted) {
        this.nonHeapMemoryCommitted = nonHeapMemoryCommitted;
    }

    public Long getNonHeapMemoryInit() {
        return nonHeapMemoryInit;
    }

    public void setNonHeapMemoryInit(Long nonHeapMemoryInit) {
        this.nonHeapMemoryInit = nonHeapMemoryInit;
    }

    public Long getNonHeapMemoryMax() {
        return nonHeapMemoryMax;
    }

    public void setNonHeapMemoryMax(Long nonHeapMemoryMax) {
        this.nonHeapMemoryMax = nonHeapMemoryMax;
    }

    public Long getNonHeapMemoryUsed() {
        return nonHeapMemoryUsed;
    }

    public void setNonHeapMemoryUsed(Long nonHeapMemoryUsed) {
        this.nonHeapMemoryUsed = nonHeapMemoryUsed;
    }

    public Long getPermGenCommitted() {
        return permGenCommitted;
    }

    public void setPermGenCommitted(Long permGenCommitted) {
        this.permGenCommitted = permGenCommitted;
    }

    public Long getPermGenInit() {
        return permGenInit;
    }

    public void setPermGenInit(Long permGenInit) {
        this.permGenInit = permGenInit;
    }

    public Long getPermGenMax() {
        return permGenMax;
    }

    public void setPermGenMax(Long permGenMax) {
        this.permGenMax = permGenMax;
    }

    public Long getPermGenUsed() {
        return permGenUsed;
    }

    public void setPermGenUsed(Long permGenUsed) {
        this.permGenUsed = permGenUsed;
    }

    public Long getOldGenCommitted() {
        return oldGenCommitted;
    }

    public void setOldGenCommitted(Long oldGenCommitted) {
        this.oldGenCommitted = oldGenCommitted;
    }

    public Long getOldGenInit() {
        return oldGenInit;
    }

    public void setOldGenInit(Long oldGenInit) {
        this.oldGenInit = oldGenInit;
    }

    public Long getOldGenMax() {
        return oldGenMax;
    }

    public void setOldGenMax(Long oldGenMax) {
        this.oldGenMax = oldGenMax;
    }

    public Long getOldGenUsed() {
        return oldGenUsed;
    }

    public void setOldGenUsed(Long oldGenUsed) {
        this.oldGenUsed = oldGenUsed;
    }

    public Long getEdenSpaceCommitted() {
        return edenSpaceCommitted;
    }

    public void setEdenSpaceCommitted(Long edenSpaceCommitted) {
        this.edenSpaceCommitted = edenSpaceCommitted;
    }

    public Long getEdenSpaceInit() {
        return edenSpaceInit;
    }

    public void setEdenSpaceInit(Long edenSpaceInit) {
        this.edenSpaceInit = edenSpaceInit;
    }

    public Long getEdenSpaceMax() {
        return edenSpaceMax;
    }

    public void setEdenSpaceMax(Long edenSpaceMax) {
        this.edenSpaceMax = edenSpaceMax;
    }

    public Long getEdenSpaceUsed() {
        return edenSpaceUsed;
    }

    public void setEdenSpaceUsed(Long edenSpaceUsed) {
        this.edenSpaceUsed = edenSpaceUsed;
    }

    public Long getSurvivorCommitted() {
        return survivorCommitted;
    }

    public void setSurvivorCommitted(Long survivorCommitted) {
        this.survivorCommitted = survivorCommitted;
    }

    public Long getSurvivorInit() {
        return survivorInit;
    }

    public void setSurvivorInit(Long survivorInit) {
        this.survivorInit = survivorInit;
    }

    public Long getSurvivorMax() {
        return survivorMax;
    }

    public void setSurvivorMax(Long survivorMax) {
        this.survivorMax = survivorMax;
    }

    public Long getSurvivorUsed() {
        return survivorUsed;
    }

    public void setSurvivorUsed(Long survivorUsed) {
        this.survivorUsed = survivorUsed;
    }
}
