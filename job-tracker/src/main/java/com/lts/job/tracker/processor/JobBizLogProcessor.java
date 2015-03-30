package com.lts.job.tracker.processor;

import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.BizLogSendRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.logger.domain.BizLogPo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobBizLogProcessor extends AbstractProcessor {

    private JobLogger jobLogger;

    public JobBizLogProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
        this.jobLogger = application.getJobLogger();
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        BizLogSendRequest requestBody = request.getBody();

        BizLogPo bizLogPo = new BizLogPo();
        bizLogPo.setJobId(requestBody.getJobId());
        bizLogPo.setTaskTrackerIdentity(requestBody.getIdentity());
        bizLogPo.setLevel(requestBody.getLevel());
        bizLogPo.setMsg(requestBody.getMsg());
        bizLogPo.setTaskTrackerNodeGroup(requestBody.getNodeGroup());
        bizLogPo.setTimestamp(requestBody.getTimestamp());
        bizLogPo.setLevel(requestBody.getLevel());

        jobLogger.log(bizLogPo);

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
    }
}
