package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.BizLog;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.BizLogSendRequest;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobBizLogProcessor extends AbstractRemotingProcessor {

    public JobBizLogProcessor(JobTrackerAppContext appContext) {
        super(appContext);
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
                jobLogPo.setRealTaskId(bizLog.getRealTaskId());
                jobLogPo.setJobType(bizLog.getJobType());
                jobLogPo.setMsg(bizLog.getMsg());
                jobLogPo.setSuccess(true);
                jobLogPo.setLevel(bizLog.getLevel());
                jobLogPo.setLogType(LogType.BIZ);
                appContext.getJobLogger().log(jobLogPo);
            }
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
    }
}
