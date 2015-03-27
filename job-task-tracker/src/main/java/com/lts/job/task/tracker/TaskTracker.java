package com.lts.job.task.tracker;

import com.lts.job.core.cluster.*;
import com.lts.job.core.constant.Constants;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.task.tracker.domain.TaskTrackerNode;
import com.lts.job.task.tracker.processor.RemotingDispatcher;
import com.lts.job.task.tracker.runner.RunnerPool;
import com.lts.job.task.tracker.support.JobPullMachine;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行节点
 */
public class TaskTracker extends AbstractClientNode<TaskTrackerNode> {

    private JobPullMachine jobPullMachine;

    public TaskTracker() {
        // 设置默认节点组
        config.setNodeGroup(Constants.DEFAULT_NODE_TASK_TRACKER_GROUP);
    }

    @Override
    protected void innerStart() {
        application.setAttribute(Constants.TASK_TRACKER_RUNNER_POOL, new RunnerPool(application));
        super.innerStart();
        jobPullMachine = new JobPullMachine(remotingClient);
        jobPullMachine.start();
    }

    @Override
    protected void innerStop() {
        super.innerStop();
        jobPullMachine.stop();
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingClient);
    }

    public void setJobRunnerClass(Class clazz) {
        application.setAttribute(Constants.JOB_RUNNING_CLASS, clazz);
    }
}
