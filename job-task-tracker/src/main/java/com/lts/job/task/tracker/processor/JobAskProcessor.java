package com.lts.job.task.tracker.processor;

import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobAskRequest;
import com.lts.job.core.protocol.command.JobAskResponse;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import com.lts.job.task.tracker.runner.RunnerPool;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskProcessor extends AbstractProcessor {

    private RunnerPool runnerPool;
    private CommandBodyWrapper commandBodyWrapper;

    protected JobAskProcessor(RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        super(remotingClient, application);
        this.runnerPool = application.getRunnerPool();
        this.commandBodyWrapper = application.getCommandBodyWrapper();
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobAskRequest requestBody = request.getBody();

        List<String> jobIds = requestBody.getJobIds();

        List<String> notExistJobIds = runnerPool.getRunningJobManager().getNotExists(jobIds);

        JobAskResponse responseBody = commandBodyWrapper.wrapper(new JobAskResponse());
        responseBody.setJobIds(notExistJobIds);

        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);
    }
}
