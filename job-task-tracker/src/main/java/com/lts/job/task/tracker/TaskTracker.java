package com.lts.job.task.tracker;

import com.lts.job.core.cluster.AbstractClientNode;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.constant.EcTopic;
import com.lts.job.core.constant.Level;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.ec.EventCenterFactory;
import com.lts.job.ec.EventInfo;
import com.lts.job.ec.EventSubscriber;
import com.lts.job.ec.Observer;
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
    private EventCenterFactory eventCenterFactory = ExtensionLoader.getExtensionLoader(EventCenterFactory.class).getAdaptiveExtension();

    public TaskTracker() {
        // 设置默认节点组
        config.setNodeGroup(Constants.DEFAULT_NODE_TASK_TRACKER_GROUP);
    }

    @Override
    protected void innerStart() {
        // 向事件中心注册事件
        eventCenterFactory.getEventCenter(config).subscribe(
                EcTopic.WORK_THREAD_CHANGE,
                new EventSubscriber(node.getIdentity(), new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        // 改变工作线程大小
                        int threads = config.getWorkThreads();
                        application.getRunnerPool().setMaximumPoolSize(threads);
                    }
                }));
        // 设置 线程池
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
    protected void nodeEnable() {
        // TODO
    }

    @Override
    protected void nodeDisable() {
        // TODO
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
