package com.lts.tasktracker;

import com.lts.core.cluster.AbstractClientNode;
import com.lts.core.constant.Level;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.tasktracker.domain.TaskTrackerApplication;
import com.lts.tasktracker.domain.TaskTrackerNode;
import com.lts.tasktracker.monitor.TaskTrackerMonitor;
import com.lts.tasktracker.processor.RemotingDispatcher;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import com.lts.tasktracker.runner.RunnerPool;
import com.lts.tasktracker.support.JobPullMachine;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行节点
 */
public class TaskTracker extends AbstractClientNode<TaskTrackerNode, TaskTrackerApplication> {

    public TaskTracker() {
        application.setMonitor(new TaskTrackerMonitor(application));
    }

    @Override
    protected void preRemotingStart() {
        // 设置 线程池
        application.setRunnerPool(new RunnerPool(application));
        application.setJobPullMachine(new JobPullMachine(application));
    }

    @Override
    protected void afterRemotingStart() {
        application.setRemotingClient(remotingClient);
        application.getMonitor().start();
    }

    @Override
    protected void afterRemotingStop() {
        application.getMonitor().stop();
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
     */
    public void setBizLoggerLevel(Level level) {
        if (level != null) {
            application.setBizLogLevel(level);
        }
    }

    /**
     * 设置JobRunner工场类，一般用户不用调用
     */
    public void setRunnerFactory(RunnerFactory factory) {
        application.setRunnerFactory(factory);
    }
}
