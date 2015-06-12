package com.lts.tasktracker.processor;

import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.protocol.command.JobAskRequest;
import com.lts.core.protocol.command.JobAskResponse;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import com.lts.tasktracker.domain.TaskTrackerApplication;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskProcessor extends AbstractProcessor {

    protected JobAskProcessor(RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        super(remotingClient, application);
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobAskRequest requestBody = request.getBody();

        List<String> jobIds = requestBody.getJobIds();

        List<String> notExistJobIds = application.getRunnerPool().getRunningJobManager().getNotExists(jobIds);

        JobAskResponse responseBody = CommandBodyWrapper.wrapper(application, new JobAskResponse());

        responseBody.setJobIds(notExistJobIds);

        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);
    }
}
