package com.lts.job.tracker;

import com.lts.job.core.cluster.*;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.util.Assert;
import com.lts.job.tracker.logger.DefaultLogger;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.queue.JobFeedbackQueue;
import com.lts.job.tracker.queue.JobQueue;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.domain.JobTrackerNode;
import com.lts.job.tracker.processor.RemotingDispatcher;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.tracker.support.listener.JobNodeChangeListener;
import com.lts.job.tracker.support.listener.JobTrackerMasterChangeListener;
import com.lts.job.tracker.support.TaskTrackerManager;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode> {

    public JobTracker() {
        config.setNodeGroup(Constants.DEFAULT_NODE_JOB_TRACKER_GROUP);
        config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
    }

    @Override
    protected void innerStart() {
        // channel 管理者
        ChannelManager channelManager = new ChannelManager();
        application.setAttribute(Constants.CHANNEL_MANAGER, channelManager);
        // JobClient 管理者
        application.setAttribute(Constants.JOB_CLIENT_MANAGER, new JobClientManager(channelManager));
        // TaskTracker 管理者
        application.setAttribute(Constants.TASK_TRACKER_MANAGER, new TaskTrackerManager(channelManager));
        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(application));
        // 设置默认 logger (如果没有设置的话)
        JobLogger jobLogger = application.getAttribute(Constants.JOB_LOGGER);
        if (jobLogger == null) {
            application.setAttribute(Constants.JOB_LOGGER, new DefaultLogger());
        }
        JobQueue jobQueue = application.getAttribute(Constants.JOB_QUEUE);
        Assert.notNull(jobQueue, "jobQueue can not be null");

        JobFeedbackQueue jobFeedbackQueue = application.getAttribute(Constants.JOB_FEEDBACK_QUEUE);
        Assert.notNull(jobFeedbackQueue, "jobFeedbackQueue can not be null");

        // 添加master节点变化监听器
        addMasterNodeChangeListener(new JobTrackerMasterChangeListener(application));
        // 启动节点
        super.innerStart();
        // 设置 remotingServer, 其他地方要用这个
        application.setAttribute(Constants.REMOTING_SERVER, remotingServer);
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingServer);
    }

    /**
     * 暂时只有JobTracker节点能记录日志，后面其他节点
     *
     * @param logger
     */
    public void setJobLogger(JobLogger logger) {
        application.setAttribute(Constants.JOB_LOGGER, logger);
    }

    public void setJobQueue(JobQueue jobQueue) {
        application.setAttribute(Constants.JOB_QUEUE, jobQueue);
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        application.setAttribute(Constants.JOB_FEEDBACK_QUEUE, jobFeedbackQueue);
    }
}
