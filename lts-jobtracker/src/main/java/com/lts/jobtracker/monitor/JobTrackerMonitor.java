package com.lts.jobtracker.monitor;

import com.lts.core.cluster.NodeType;
import com.lts.core.domain.monitor.JobTrackerMonitorData;
import com.lts.core.monitor.AbstractMonitor;
import com.lts.core.domain.monitor.MonitorData;
import com.lts.jobtracker.domain.JobTrackerAppContext;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 8/31/15.
 */
public class JobTrackerMonitor extends AbstractMonitor {

    public JobTrackerMonitor(JobTrackerAppContext appContext) {
        super(appContext);
    }

    // 接受的任务数
    private AtomicLong receiveJobNum = new AtomicLong(0);
    // 分发出去的任务数
    private AtomicLong pushJobNum = new AtomicLong(0);
    // 执行成功个数
    private AtomicLong exeSuccessNum = new AtomicLong(0);
    // 执行失败个数
    private AtomicLong exeFailedNum = new AtomicLong(0);
    // 延迟执行个数
    private AtomicLong exeLaterNum = new AtomicLong(0);
    // 执行异常个数
    private AtomicLong exeExceptionNum = new AtomicLong(0);
    // 修复死任务数
    private AtomicLong fixExecutingJobNum = new AtomicLong(0);

    public void incReceiveJobNum(){
        receiveJobNum.incrementAndGet();
    }

    public void incPushJobNum(){
        pushJobNum.incrementAndGet();
    }

    public void incExeSuccessNum(){
        exeSuccessNum.incrementAndGet();
    }

    public void incExeFailedNum(){
        exeFailedNum.incrementAndGet();
    }

    public void incExeLaterNum(){
        exeLaterNum.incrementAndGet();
    }

    public void incExeExceptionNum(){
        exeExceptionNum.incrementAndGet();
    }

    public void incFixExecutingJobNum(){
        fixExecutingJobNum.incrementAndGet();
    }

    @Override
    protected MonitorData collectMonitorData() {
        JobTrackerMonitorData monitorData = new JobTrackerMonitorData();
        monitorData.setReceiveJobNum(receiveJobNum.getAndSet(0));
        monitorData.setExeExceptionNum(exeExceptionNum.getAndSet(0));
        monitorData.setExeFailedNum(exeFailedNum.getAndSet(0));
        monitorData.setExeSuccessNum(exeSuccessNum.getAndSet(0));
        monitorData.setExeLaterNum(exeLaterNum.getAndSet(0));
        monitorData.setFixExecutingJobNum(fixExecutingJobNum.getAndSet(0));
        monitorData.setPushJobNum(pushJobNum.getAndSet(0));
        return monitorData;
    }

    @Override
    protected NodeType getNodeType() {
        return NodeType.JOB_TRACKER;
    }
}
