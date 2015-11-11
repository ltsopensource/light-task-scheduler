package com.lts.jobclient.processor;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobFinishedRequest;
import com.lts.jobclient.domain.JobClientApplication;
import com.lts.remoting.Channel;
import com.lts.remoting.RemotingProcessor;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobFinishedProcessor implements RemotingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class);

    private JobClientApplication application;

    public JobFinishedProcessor(JobClientApplication application) {
        this.application = application;
    }

    @Override
    public RemotingCommand processRequest(Channel Channel, RemotingCommand request)
            throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();
        try {
            if (application.getJobCompletedHandler() != null) {
                application.getJobCompletedHandler().onComplete(requestBody.getJobResults());
            }
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code(),
                "received successful");
    }
}
