package com.lts.job.task.tracker;

import com.lts.job.core.cluster.AbstractClientNode;
import com.lts.job.core.constant.Level;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.domain.TaskTrackerNode;
import com.lts.job.task.tracker.processor.RemotingDispatcher;
import com.lts.job.task.tracker.runner.JobRunner;
import com.lts.job.task.tracker.runner.RunnerPool;
import com.lts.job.task.tracker.support.JobPullMachine;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行节点
 */
public class TaskTracker extends AbstractClientNode<TaskTrackerNode, TaskTrackerApplication> {

    @Override
    protected void innerStart() {
        // 设置 线程池
        application.setRunnerPool(new RunnerPool(application));
        application.setJobPullMachine(new JobPullMachine(application));
    }

    @Override
    protected void injectRemotingClient() {
        application.setRemotingClient(remotingClient);
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingClient, application);
    }

    public <JRC extends JobRunner> void setJobRunnerClass(Class<JRC> clazz) {
        application.setJobRunnerClass(clazz);
    }

    public void setWorkThreads(int workThreads) {
        config.setWorkThreads(workThreads);
    }

    /**
     * 设置业务日志记录级别
     *
     * @param level
     */
    public void setBizLoggerLevel(Level level) {
        if (level != null) {
            application.setBizLogLevel(level);
        }
    }
}
