package com.lts.jobtracker.processor;

import com.lts.core.exception.JobReceiveException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.core.protocol.command.JobSubmitResponse;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.remoting.Channel;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         客户端提交任务的处理器
 */
public class JobSubmitProcessor extends AbstractRemotingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSubmitProcessor.class);

    public JobSubmitProcessor(JobTrackerAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        JobSubmitRequest jobSubmitRequest = request.getBody();

        JobSubmitResponse jobSubmitResponse = appContext.getCommandBodyWrapper().wrapper(new JobSubmitResponse());
        RemotingCommand response = null;
        try {
            appContext.getJobReceiver().receive(jobSubmitRequest);

            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code(), "job submit success!", jobSubmitResponse);

        } catch (JobReceiveException e) {
            LOGGER.error("Receive job failed , jobs = " + jobSubmitRequest.getJobs(), e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
