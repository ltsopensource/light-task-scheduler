package com.lts.jobclient.processor;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobFinishedRequest;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.jobclient.support.JobFinishedHandler;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobFinishedProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class);

    private JobFinishedHandler jobFinishedHandler;

    public JobFinishedProcessor(RemotingClientDelegate remotingClient,
                                JobFinishedHandler jobFinishedHandler) {
        super(remotingClient);
        this.jobFinishedHandler = jobFinishedHandler;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();
        try {
            if (jobFinishedHandler != null) {
                jobFinishedHandler.handle(requestBody.getJobResults());
            }
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code(), "received successful");
    }
}
