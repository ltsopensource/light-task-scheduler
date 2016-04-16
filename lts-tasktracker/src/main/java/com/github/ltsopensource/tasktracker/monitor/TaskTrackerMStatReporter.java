package com.github.ltsopensource.tasktracker.monitor;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.domain.monitor.TaskTrackerMData;
import com.github.ltsopensource.core.monitor.AbstractMStatReporter;
import com.github.ltsopensource.core.domain.monitor.MData;

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
public class TaskTrackerMStatReporter extends AbstractMStatReporter {

    // 执行成功个数
    private AtomicLong exeSuccessNum = new AtomicLong(0);
    // 执行失败个数
    private AtomicLong exeFailedNum = new AtomicLong(0);
    // 延迟执行个数
    private AtomicLong exeLaterNum = new AtomicLong(0);
    // 执行异常个数
    private AtomicLong exeExceptionNum = new AtomicLong(0);
    // 总的运行时间
    private AtomicLong totalRunningTime = new AtomicLong(0);

    public TaskTrackerMStatReporter(AppContext appContext) {
        super(appContext);
    }

    public void incSuccessNum() {
        exeSuccessNum.incrementAndGet();
    }

    public void incFailedNum() {
        exeFailedNum.incrementAndGet();
    }

    public void incExeLaterNum() {
        exeLaterNum.incrementAndGet();
    }

    public void incExeExceptionNum() {
        exeExceptionNum.incrementAndGet();
    }

    public void addRunningTime(Long time) {
        totalRunningTime.addAndGet(time);
    }

    @Override
    protected MData collectMData() {
        TaskTrackerMData mData = new TaskTrackerMData();
        mData.setExeSuccessNum(exeSuccessNum.getAndSet(0));
        mData.setExeFailedNum(exeFailedNum.getAndSet(0));
        mData.setExeLaterNum(exeLaterNum.getAndSet(0));
        mData.setExeExceptionNum(exeExceptionNum.getAndSet(0));
        mData.setTotalRunningTime(totalRunningTime.getAndSet(0));
        return mData;
    }

    @Override
    protected NodeType getNodeType() {
        return NodeType.TASK_TRACKER;
    }

}
