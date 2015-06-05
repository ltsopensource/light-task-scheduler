package com.lts.job.tracker.processor;

import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.tracker.domain.JobTrackerApplication;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor{

    protected RemotingServerDelegate remotingServer;
    protected JobTrackerApplication application;

    public AbstractProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        this.remotingServer = remotingServer;
        this.application = application;
    }

}
