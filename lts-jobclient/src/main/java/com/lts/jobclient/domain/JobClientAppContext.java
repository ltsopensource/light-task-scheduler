package com.lts.jobclient.domain;

import com.lts.core.AppContext;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.jobclient.support.JobCompletedHandler;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobClientAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;

    private JobCompletedHandler jobCompletedHandler;

    public JobCompletedHandler getJobCompletedHandler() {
        return jobCompletedHandler;
    }

    public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
        this.jobCompletedHandler = jobCompletedHandler;
    }

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}

