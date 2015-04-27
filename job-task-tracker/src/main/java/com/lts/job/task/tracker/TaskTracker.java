package com.lts.job.task.tracker;

import com.lts.job.core.cluster.AbstractClientNode;
import com.lts.job.core.constant.Constants;
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

    private JobPullMachine jobPullMachine;

    public TaskTracker() {
        // 设置默认节点组
        config.setNodeGroup(Constants.DEFAULT_NODE_TASK_TRACKER_GROUP);
    }

    @Override
    protected void innerStart() {
        application.setRunnerPool(new RunnerPool(application));
        super.innerStart();
        application.setRemotingClient(remotingClient);
        jobPullMachine = new JobPullMachine(application);
        jobPullMachine.start();
    }

    @Override
    protected void innerStop() {
        super.innerStop();
        jobPullMachine.stop();
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
        application.setAvailableThreads(config.getWorkThreads());
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
