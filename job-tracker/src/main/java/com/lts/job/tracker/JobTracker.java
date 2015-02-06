package com.lts.job.tracker;

import com.lts.job.common.cluster.*;
import com.lts.job.common.constant.Constants;
import com.lts.job.common.support.SingletonBeanContext;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.store.Config;
import com.lts.job.store.mongo.DatastoreHolder;
import com.lts.job.tracker.domain.JobTrackerNode;
import com.lts.job.tracker.processor.RemotingDispatcher;
import com.lts.job.tracker.support.DeadJobChecker;
import com.lts.job.tracker.support.JobNodeChangeListener;
import com.lts.job.tracker.support.JobTrackerMasterChangeListener;
import com.lts.job.tracker.support.RemotingServerManager;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode> {

    private DeadJobChecker deadJobChecker;

    public JobTracker() {
        config.setNodeGroup(Constants.DEFAULT_NODE_JOB_TRACKER_GROUP);
        config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
        addNodeChangeListener(new JobNodeChangeListener());
        addMasterNodeChangeListener(new JobTrackerMasterChangeListener());
    }

    @Override
    protected void nodeStart() {
        super.nodeStart();
        // 改为这里初始化, 主要是为了 让 mongo延迟初始化，可以让设置的 storeConfig起作用
        deadJobChecker = SingletonBeanContext.getBean(DeadJobChecker.class);
        deadJobChecker.start();

        RemotingServerManager.setRemotingServer(remotingServer);
    }

    @Override
    protected void nodeStop() {
        super.nodeStop();
        deadJobChecker.stop();
    }

    @Override
    protected NettyRequestProcessor getDefaultProcessor() {
        return new RemotingDispatcher(remotingServer);
    }

    /**
     * 设置存储配置 (这里是mongo，也可以配置mongo.properties)
     *
     * @param config
     */
    public void setStoreConfig(Config config) {
        DatastoreHolder.setConfig(config);
    }

}
