package com.lts.job.tracker.processor;

import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.LogType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.BizLogSendRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobBizLogProcessor extends AbstractProcessor {

    public JobBizLogProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        BizLogSendRequest requestBody = request.getBody();

        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setTimestamp(requestBody.getTimestamp());
        jobLogPo.setTaskTrackerNodeGroup(requestBody.getNodeGroup());
        jobLogPo.setTaskTrackerIdentity(requestBody.getIdentity());
        jobLogPo.setJobId(requestBody.getJobId());
        jobLogPo.setTaskId(requestBody.getTaskId());
        jobLogPo.setMsg(requestBody.getMsg());
        jobLogPo.setSuccess(true);
        jobLogPo.setLevel(requestBody.getLevel());
        jobLogPo.setLogType(LogType.BIZ);

        application.getJobLogger().log(jobLogPo);

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
    }
}
