package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.jobtracker.complete.biz.JobCompletedBiz;
import com.github.ltsopensource.jobtracker.complete.biz.JobProcBiz;
import com.github.ltsopensource.jobtracker.complete.biz.JobStatBiz;
import com.github.ltsopensource.jobtracker.complete.biz.PushNewJobBiz;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;

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
