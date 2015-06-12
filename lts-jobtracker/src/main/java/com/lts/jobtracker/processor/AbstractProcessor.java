package com.lts.jobtracker.processor;

import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.jobtracker.domain.JobTrackerApplication;

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
