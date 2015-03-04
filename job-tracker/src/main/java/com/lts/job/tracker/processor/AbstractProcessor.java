package com.lts.job.tracker.processor;

import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.core.repository.JobMongoRepository;
import com.lts.job.core.support.SingletonBeanContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor{

    protected RemotingServerDelegate remotingServer;
    protected JobMongoRepository jobRepository;

    public AbstractProcessor(RemotingServerDelegate remotingServer) {
        this.remotingServer = remotingServer;
        this.jobRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
    }

}
