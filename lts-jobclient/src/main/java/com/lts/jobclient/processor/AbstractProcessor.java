package com.lts.jobclient.processor;

import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.remoting.netty.NettyRequestProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor{

    protected RemotingClientDelegate remotingClient;

    protected AbstractProcessor(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}
