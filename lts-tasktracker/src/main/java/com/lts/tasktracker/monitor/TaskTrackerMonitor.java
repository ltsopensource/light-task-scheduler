package com.lts.tasktracker.monitor;

import com.lts.core.Application;
import com.lts.core.cluster.NodeType;
import com.lts.core.domain.TaskTrackerMonitorData;
import com.lts.core.monitor.AbstractMonitor;
import com.lts.core.monitor.MonitorData;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 主要用来监控TaskTracker的压力
 * 1. 任务执行量，任务执行成功数，任务执行失败数
 * 2. FailStore 容量
 * 3. 内存占用情况
 * 定时向 monitor 发送，方便生成图表在LTS-Admin查看，预警等
 *
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class TaskTrackerMonitor extends AbstractMonitor {

    // 执行成功的个数
    private AtomicLong successNum = new AtomicLong(0);
    // 执行失败的个数
    private AtomicLong failedNum = new AtomicLong(0);
    // 总的运行时间
    private AtomicLong totalRunningTime = new AtomicLong(0);

    public TaskTrackerMonitor(Application application) {
        super(application);
    }

    public void increaseSuccessNum() {
        successNum.incrementAndGet();
    }

    public void increaseFailedNum() {
        failedNum.incrementAndGet();
    }

    public void addRunningTime(Long time) {
        totalRunningTime.addAndGet(time);
    }

    @Override
    protected MonitorData collectMonitorData() {
        TaskTrackerMonitorData monitorData = new TaskTrackerMonitorData();
        monitorData.setSuccessNum(successNum.getAndSet(0));
        monitorData.setFailedNum(failedNum.getAndSet(0));
        monitorData.setTotalRunningTime(totalRunningTime.getAndSet(0));
        monitorData.setFailStoreSize(getFailStoreSize());
        return monitorData;
    }

    @Override
    protected NodeType getNodeType() {
        return NodeType.TASK_TRACKER;
    }

}
