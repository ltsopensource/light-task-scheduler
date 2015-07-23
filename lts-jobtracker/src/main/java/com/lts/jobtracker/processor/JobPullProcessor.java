package com.lts.jobtracker.processor;

import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobPullRequest;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.support.JobPusher;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         处理 TaskTracker的 Job pull 请求
 */
public class JobPullProcessor extends AbstractProcessor {

    private JobPusher jobPusher;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPullProcessor.class);

    public JobPullProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);

        jobPusher = new JobPusher(application);
    }

    @Override
    public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request) throws RemotingCommandException {

        JobPullRequest requestBody = request.getBody();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:{}", requestBody.getNodeGroup(), requestBody.getIdentity(), requestBody.getAvailableThreads());
        }
        jobPusher.push(remotingServer, requestBody);

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_PULL_SUCCESS.code(), "");
    }
}
