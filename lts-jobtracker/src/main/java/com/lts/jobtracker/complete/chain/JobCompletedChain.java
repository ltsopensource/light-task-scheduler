package com.lts.jobtracker.complete.chain;

import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobCompletedChain {

    /**
     * 如果返回空表示继续执行
     */
    RemotingCommand doChain(JobCompletedRequest request);

}
