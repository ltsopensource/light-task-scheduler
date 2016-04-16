package com.github.ltsopensource.tasktracker.processor;

import com.github.ltsopensource.core.protocol.command.CommandBodyWrapper;
import com.github.ltsopensource.core.protocol.command.JobAskRequest;
import com.github.ltsopensource.core.protocol.command.JobAskResponse;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

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
