package com.lts.job.client.processor;

import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.remoting.netty.NettyRequestProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor{

    protected RemotingClientDelegate remotingClient;

    protected AbstractProcessor(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}
