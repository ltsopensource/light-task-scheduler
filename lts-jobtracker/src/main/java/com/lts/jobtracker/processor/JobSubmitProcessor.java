package com.lts.jobtracker.processor;

import com.lts.core.exception.JobReceiveException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobSubmitRequest;
import com.lts.core.protocol.command.JobSubmitResponse;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.support.JobReceiver;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         客户端提交任务的处理器
 */
public class JobSubmitProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSubmitProcessor.class);

    private JobReceiver jobReceiver;

    public JobSubmitProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
        this.jobReceiver = new JobReceiver(application);
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobSubmitRequest jobSubmitRequest = request.getBody();

        JobSubmitResponse jobSubmitResponse = application.getCommandBodyWrapper().wrapper(new JobSubmitResponse());
        RemotingCommand response = null;
        try {
            jobReceiver.receive(jobSubmitRequest);

            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code(), "job submit success!", jobSubmitResponse);

        } catch (JobReceiveException e) {
            LOGGER.error("receive job failed , jobs = " + jobSubmitRequest.getJobs(), e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
