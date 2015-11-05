package com.lts.jobtracker.processor;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.domain.BizLog;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.BizLogSendRequest;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.remoting.Channel;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobBizLogProcessor extends AbstractRemotingProcessor {

    public JobBizLogProcessor(JobTrackerApplication application) {
        super(application);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        BizLogSendRequest requestBody = request.getBody();

        List<BizLog> bizLogs = requestBody.getBizLogs();
        if (CollectionUtils.isNotEmpty(bizLogs)) {
            for (BizLog bizLog : bizLogs) {
                JobLogPo jobLogPo = new JobLogPo();
                jobLogPo.setGmtCreated(SystemClock.now());
                jobLogPo.setLogTime(bizLog.getLogTime());
                jobLogPo.setTaskTrackerNodeGroup(bizLog.getTaskTrackerNodeGroup());
                jobLogPo.setTaskTrackerIdentity(bizLog.getTaskTrackerIdentity());
                jobLogPo.setJobId(bizLog.getJobId());
                jobLogPo.setTaskId(bizLog.getTaskId());
                jobLogPo.setMsg(bizLog.getMsg());
                jobLogPo.setSuccess(true);
                jobLogPo.setLevel(bizLog.getLevel());
                jobLogPo.setLogType(LogType.BIZ);
                application.getJobLogger().log(jobLogPo);
            }
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
    }
}
