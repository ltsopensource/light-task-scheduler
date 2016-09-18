package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.RemotingProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractRemotingProcessor implements RemotingProcessor {

    protected JobTrackerAppContext appContext;

    public AbstractRemotingProcessor(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

}
