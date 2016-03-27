package com.lts.jobtracker.complete.biz;

import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobCompletedBiz {

    /**
     * 如果返回空表示继续执行
     */
    RemotingCommand doBiz(JobCompletedRequest request);

}
