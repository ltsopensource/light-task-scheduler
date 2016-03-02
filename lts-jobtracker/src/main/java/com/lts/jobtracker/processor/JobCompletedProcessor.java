package com.lts.jobtracker.processor;

import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.jobtracker.complete.chain.GetNewJobChain;
import com.lts.jobtracker.complete.chain.JobCompletedChain;
import com.lts.jobtracker.complete.chain.JobProcessChain;
import com.lts.jobtracker.complete.chain.JobStatisticChain;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.remoting.Channel;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         TaskTracker 完成任务 的处理器
 */
public class JobCompletedProcessor extends AbstractRemotingProcessor {

    private List<JobCompletedChain> chains;

    public JobCompletedProcessor(final JobTrackerAppContext appContext) {
        super(appContext);

        this.chains = new CopyOnWriteArrayList<JobCompletedChain>();
        this.chains.add(new JobStatisticChain(appContext));        // 统计
        this.chains.add(new JobProcessChain(appContext));          // 完成处理
        this.chains.add(new GetNewJobChain(appContext));           // 获取新任务

    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request)
            throws RemotingCommandException {

        JobCompletedRequest requestBody = request.getBody();

        // chain invoke
        for (JobCompletedChain chain : chains) {
            RemotingCommand remotingCommand = chain.doChain(requestBody);
            if (remotingCommand != null) {
                return remotingCommand;
            }
        }
        return RemotingCommand.createResponseCommand(RemotingProtos
                .ResponseCode.SUCCESS.code());
    }

}
