package com.lts.job.tracker.processor;

import com.lts.job.core.exception.JobReceiveException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobSubmitRequest;
import com.lts.job.core.protocol.command.JobSubmitResponse;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.support.JobReceiver;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 客户端提交任务的处理器
 */
public class JobSubmitProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSubmitProcessor.class);

    private CommandBodyWrapper commandBodyWrapper;
    private JobReceiver jobReceiver;

    public JobSubmitProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
        this.commandBodyWrapper = application.getCommandBodyWrapper();
        this.jobReceiver = new JobReceiver(application.getJobQueue());
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobSubmitRequest jobSubmitRequest =  request.getBody();

        JobSubmitResponse jobSubmitResponse = commandBodyWrapper.wrapper(new JobSubmitResponse());
        RemotingCommand response = null;
        try {
            jobReceiver.receive(jobSubmitRequest);

            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code(), "job submit success!", jobSubmitResponse);

        } catch (JobReceiveException e) {
            LOGGER.error("receive job failed , " + jobSubmitRequest, e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
