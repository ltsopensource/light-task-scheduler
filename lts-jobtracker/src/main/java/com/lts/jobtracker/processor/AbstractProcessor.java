package com.lts.jobtracker.processor;

import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.remoting.netty.NettyRequestProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor{

    protected JobTrackerApplication application;

    public AbstractProcessor(JobTrackerApplication application) {
        this.application = application;
    }

}
