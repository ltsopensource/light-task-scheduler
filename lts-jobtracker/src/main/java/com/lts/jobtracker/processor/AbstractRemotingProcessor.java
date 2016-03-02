package com.lts.jobtracker.processor;

import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.remoting.RemotingProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractRemotingProcessor implements RemotingProcessor {

    protected JobTrackerAppContext appContext;

    public AbstractRemotingProcessor(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

}
