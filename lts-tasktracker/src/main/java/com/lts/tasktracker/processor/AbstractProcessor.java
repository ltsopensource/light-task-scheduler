package com.lts.tasktracker.processor;

import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.tasktracker.domain.TaskTrackerApplication;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor {

    protected RemotingClientDelegate remotingClient;
    protected TaskTrackerApplication application;

    protected AbstractProcessor(RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        this.remotingClient = remotingClient;
        this.application = application;
    }
}
