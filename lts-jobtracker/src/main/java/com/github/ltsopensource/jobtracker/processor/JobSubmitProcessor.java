package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.core.exception.JobReceiveException;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobSubmitRequest;
import com.github.ltsopensource.core.protocol.command.JobSubmitResponse;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

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
        RemotingCommand response;
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
