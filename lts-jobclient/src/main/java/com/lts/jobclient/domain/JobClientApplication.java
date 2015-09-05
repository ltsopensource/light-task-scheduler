package com.lts.jobclient.domain;

import com.lts.core.Application;
import com.lts.core.remoting.RemotingClientDelegate;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobClientApplication extends Application{

    private RemotingClientDelegate remotingClient;

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}

