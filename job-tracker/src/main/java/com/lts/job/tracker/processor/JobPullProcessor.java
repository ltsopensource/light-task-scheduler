package com.lts.job.tracker.processor;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.factory.NamedThreadFactory;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobPullRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.support.JobDistributor;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         处理 TaskTracker的 Job pull 请求
 */
public class JobPullProcessor extends AbstractProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobPullProcessor.class.getSimpleName());

    private final ExecutorService executor;
    private JobDistributor jobDistributor;

    public JobPullProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);

        executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 5
                , new NamedThreadFactory(JobPullProcessor.class.getSimpleName()));
        jobDistributor = new JobDistributor(application);
    }

    @Override
    public RemotingCommand processRequest(final ChannelHandlerContext ctx, final RemotingCommand request) throws RemotingCommandException {

        final JobPullRequest requestBody = request.getBody();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    jobDistributor.pushJob(remotingServer, requestBody);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_PULL_SUCCESS.code(), "");
    }
}
