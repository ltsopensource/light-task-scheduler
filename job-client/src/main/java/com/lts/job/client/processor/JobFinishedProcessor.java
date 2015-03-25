package com.lts.job.client.processor;

import com.lts.job.client.support.JobFinishedHandler;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 */
public class JobFinishedProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class);

    private JobFinishedHandler jobFinishedHandler;

    public JobFinishedProcessor(RemotingClientDelegate remotingClient, JobFinishedHandler jobFinishedHandler) {
        super(remotingClient);
        this.jobFinishedHandler = jobFinishedHandler;
        if (this.jobFinishedHandler == null) {
            this.jobFinishedHandler = new JobFinishedHandler() {
                private final Logger log = LoggerFactory.getLogger("JobFinishedHandler");

                @Override
                public void handle(List<JobResult> jobResults) {
                    // do nothing
                    if (CollectionUtils.isNotEmpty(jobResults)) {
                        for (JobResult jobResult : jobResults) {
                            if (jobResult.isSuccess()) {
                                log.info("任务执行成功:" + jobResult);
                            } else {
                                log.info("任务执行失败:" + jobResult);
                            }
                        }
                    }
                }
            };
        }
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();
        try {
            jobFinishedHandler.handle(requestBody.getJobResults());
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code(), "接受成功");
    }
}
