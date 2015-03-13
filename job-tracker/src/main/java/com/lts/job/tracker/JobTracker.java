package com.lts.job.tracker;

import com.lts.job.core.cluster.*;
import com.lts.job.core.constant.Constants;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.store.Config;
import com.lts.job.store.mongo.DatastoreHolder;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.domain.JobTrackerNode;
import com.lts.job.tracker.processor.RemotingDispatcher;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.tracker.support.JobNodeChangeListener;
import com.lts.job.tracker.support.JobTrackerMasterChangeListener;
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
    protected void nodeStart() {

        ChannelManager channelManager = new ChannelManager();
        application.setAttribute(Constants.CHANNEL_MANAGER, channelManager);
        application.setAttribute(Constants.JOB_CLIENT_MANAGER, new JobClientManager(channelManager));
        application.setAttribute(Constants.TASK_TRACKER_MANAGER, new TaskTrackerManager(channelManager));
        addNodeChangeListener(new JobNodeChangeListener(application));
        addMasterNodeChangeListener(new JobTrackerMasterChangeListener(application));

        super.nodeStart();
        application.setAttribute(Constants.REMOTING_SERVER, remotingServer);
    }

    @Override
    protected void nodeStop() {
        super.nodeStop();
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingServer);
    }

    /**
     * 设置存储配置
     *
     * @param config
     */
    public void setStoreConfig(Config config) {
        DatastoreHolder.setConfig(config);
    }

}
