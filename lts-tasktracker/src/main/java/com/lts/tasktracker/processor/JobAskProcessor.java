package com.lts.tasktracker.processor;

import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.protocol.command.JobAskRequest;
import com.lts.core.protocol.command.JobAskResponse;
import com.lts.remoting.Channel;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import com.lts.tasktracker.domain.TaskTrackerAppContext;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskProcessor extends AbstractProcessor {

    protected JobAskProcessor(TaskTrackerAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          RemotingCommand request) throws RemotingCommandException {

        JobAskRequest requestBody = request.getBody();

        List<String> jobIds = requestBody.getJobIds();

        List<String> notExistJobIds = appContext.getRunnerPool()
                .getRunningJobManager().getNotExists(jobIds);

        JobAskResponse responseBody = CommandBodyWrapper.wrapper(appContext, new JobAskResponse());

        responseBody.setJobIds(notExistJobIds);

        return RemotingCommand.createResponseCommand(
                RemotingProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);
    }
}
