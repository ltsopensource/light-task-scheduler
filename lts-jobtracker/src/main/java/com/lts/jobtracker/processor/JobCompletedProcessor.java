package com.lts.jobtracker.processor;

import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.jobtracker.complete.biz.JobCompletedBiz;
import com.lts.jobtracker.complete.biz.JobProcBiz;
import com.lts.jobtracker.complete.biz.JobStatBiz;
import com.lts.jobtracker.complete.biz.PushNewJobBiz;
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

    private List<JobCompletedBiz> bizChain;

    public JobCompletedProcessor(final JobTrackerAppContext appContext) {
        super(appContext);

        this.bizChain = new CopyOnWriteArrayList<JobCompletedBiz>();
        this.bizChain.add(new JobStatBiz(appContext));        // 统计
        this.bizChain.add(new JobProcBiz(appContext));          // 完成处理
        this.bizChain.add(new PushNewJobBiz(appContext));           // 获取新任务

    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request)
            throws RemotingCommandException {

        JobCompletedRequest requestBody = request.getBody();

        for (JobCompletedBiz biz : bizChain) {
            RemotingCommand remotingCommand = biz.doBiz(requestBody);
            if (remotingCommand != null) {
                return remotingCommand;
            }
        }
        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code());
    }

}
