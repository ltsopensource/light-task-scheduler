package com.lts.jobclient.domain;

import com.lts.core.Application;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.jobclient.support.JobFinishedHandler;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobClientApplication extends Application{

    private RemotingClientDelegate remotingClient;

    private JobFinishedHandler jobFinishedHandler;

    public JobFinishedHandler getJobFinishedHandler() {
        return jobFinishedHandler;
    }

    public void setJobFinishedHandler(JobFinishedHandler jobFinishedHandler) {
        this.jobFinishedHandler = jobFinishedHandler;
    }

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}

