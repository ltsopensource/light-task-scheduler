package com.github.ltsopensource.jobtracker.complete.biz;

import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobCompletedBiz {

    /**
     * 如果返回空表示继续执行
     */
    RemotingCommand doBiz(JobCompletedRequest request);

}
