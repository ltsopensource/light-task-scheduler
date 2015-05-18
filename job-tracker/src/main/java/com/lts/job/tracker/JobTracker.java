package com.lts.job.tracker;

import com.lts.job.core.cluster.AbstractServerNode;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.util.Assert;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.domain.JobTrackerNode;
import com.lts.job.tracker.logger.DefaultLogger;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.processor.RemotingDispatcher;
import com.lts.job.tracker.queue.JobFeedbackQueue;
import com.lts.job.tracker.queue.JobQueue;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.tracker.support.OldDataHandler;
import com.lts.job.tracker.support.TaskTrackerManager;
import com.lts.job.tracker.support.listener.JobNodeChangeListener;
import com.lts.job.tracker.support.listener.JobTrackerMasterChangeListener;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerApplication> {

    public JobTracker() {

        config.setNodeGroup(Constants.DEFAULT_NODE_JOB_TRACKER_GROUP);
        config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(application));
        // channel 管理者
        ChannelManager channelManager = new ChannelManager();
        application.setChannelManager(channelManager);
        // JobClient 管理者
        application.setJobClientManager(new JobClientManager(application));
        // TaskTracker 管理者
        application.setTaskTrackerManager(new TaskTrackerManager(application));
    }

    @Override
    protected void innerStart() {

        application.getChannelManager().start();

        // 设置默认 logger (如果没有设置的话)
        JobLogger jobLogger = application.getJobLogger();
        if (jobLogger == null) {
            application.setJobLogger(new DefaultLogger());
        }
        JobQueue jobQueue = application.getJobQueue();
        Assert.notNull(jobQueue, "jobQueue can not be null");

        JobFeedbackQueue jobFeedbackQueue = application.getJobFeedbackQueue();
        Assert.notNull(jobFeedbackQueue, "jobFeedbackQueue can not be null");

        // 添加master节点变化监听器
        addMasterChangeListener(new JobTrackerMasterChangeListener(application));
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
     * 暂时只有JobTracker节点能记录日志，后面其他节点
     *
     * @param logger
     */
    public void setJobLogger(JobLogger logger) {
        application.setJobLogger(logger);
    }

    public void setJobQueue(JobQueue jobQueue) {
        application.setJobQueue(jobQueue);
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        application.setJobFeedbackQueue(jobFeedbackQueue);
    }

    /**
     * 设置反馈数据给JobClient的负载均衡算法
     * @param loadBalance
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        application.setOldDataHandler(oldDataHandler);
    }
}
