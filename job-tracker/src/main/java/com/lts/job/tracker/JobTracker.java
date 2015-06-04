package com.lts.job.tracker;

import com.lts.job.biz.logger.JobLoggerFactory;
import com.lts.job.core.cluster.AbstractServerNode;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.queue.CronJobQueueFactory;
import com.lts.job.queue.ExecutableJobQueueFactory;
import com.lts.job.queue.ExecutingJobQueueFactory;
import com.lts.job.queue.JobFeedbackQueueFactory;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.domain.JobTrackerNode;
import com.lts.job.tracker.processor.RemotingDispatcher;
import com.lts.job.tracker.support.OldDataHandler;
import com.lts.job.tracker.support.cluster.JobClientManager;
import com.lts.job.tracker.support.cluster.TaskTrackerManager;
import com.lts.job.tracker.support.listener.JobNodeChangeListener;
import com.lts.job.tracker.support.listener.JobTrackerMasterChangeListener;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerApplication> {

    private JobLoggerFactory jobLoggerFactory = ExtensionLoader.getExtensionLoader(JobLoggerFactory.class).getAdaptiveExtension();

    private CronJobQueueFactory cronJobQueueFactory = ExtensionLoader.getExtensionLoader(CronJobQueueFactory.class).getAdaptiveExtension();
    private ExecutableJobQueueFactory executableJobQueueFactory = ExtensionLoader.getExtensionLoader(ExecutableJobQueueFactory.class).getAdaptiveExtension();
    private ExecutingJobQueueFactory executingJobQueueFactory = ExtensionLoader.getExtensionLoader(ExecutingJobQueueFactory.class).getAdaptiveExtension();

    private JobFeedbackQueueFactory jobFeedbackQueueFactory = ExtensionLoader.getExtensionLoader(JobFeedbackQueueFactory.class).getAdaptiveExtension();

    public JobTracker() {
        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(application));
        // channel 管理者
        application.setChannelManager(new ChannelManager());
        // JobClient 管理者
        application.setJobClientManager(new JobClientManager(application));
        // TaskTracker 管理者
        application.setTaskTrackerManager(new TaskTrackerManager(application));

        // 添加master节点变化监听器
        addMasterChangeListener(new JobTrackerMasterChangeListener(application));
    }

    @Override
    protected void innerStart() {

        application.setJobLogger(jobLoggerFactory.getJobLogger(config));

        application.setExecutableJobQueue(executableJobQueueFactory.getQueue(config));
        application.setExecutingJobQueue(executingJobQueueFactory.getQueue(config));
        application.setCronJobQueue(cronJobQueueFactory.getQueue(config));
        application.setJobFeedbackQueue(jobFeedbackQueueFactory.getQueue(config));

        application.getChannelManager().start();
        // 启动节点
        super.innerStart();

        // 设置 remotingServer, 其他地方要用这个
        application.setRemotingServer(remotingServer);
    }

    @Override
    protected void innerStop() {
        super.innerStop();
        application.getChannelManager().start();
    }

    @Override
    protected void nodeEnable() {
        // TODO

    }

    @Override
    protected void nodeDisable() {
        // TODO 节点被禁用
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingServer, application);
    }

    /**
     * 设置反馈数据给JobClient的负载均衡算法
     *
     * @param loadBalance
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        application.setOldDataHandler(oldDataHandler);
    }

}
