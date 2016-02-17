package com.lts.jobtracker.complete.chain;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.constant.Level;
import com.lts.core.domain.Action;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.JobCompletedRequest;
import com.lts.core.support.LoggerName;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.jobtracker.monitor.JobTrackerMonitor;
import com.lts.jobtracker.support.JobDomainConverter;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;

import java.util.List;

/**
 * 任务数据统计 Chain
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobStatisticChain implements JobCompletedChain {

    private final Logger LOGGER = LoggerFactory.getLogger(LoggerName.JobTracker);

    private JobTrackerAppContext appContext;
    private JobTrackerMonitor monitor;

    public JobStatisticChain(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.monitor = (JobTrackerMonitor) appContext.getMonitor();

    }

    @Override
    public RemotingCommand doChain(JobCompletedRequest request) {

        List<TaskTrackerJobResult> results = request.getTaskTrackerJobResults();

        if (CollectionUtils.isEmpty(results)) {
            return RemotingCommand.createResponseCommand(RemotingProtos
                            .ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobResults can not be empty!");
        }

        LOGGER.info("Job execute completed : {}", results);

        LogType logType = request.isReSend() ? LogType.RESEND : LogType.FINISHED;

        for (TaskTrackerJobResult result : results) {

            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(result.getJobWrapper());
            jobLogPo.setMsg(result.getMsg());
            jobLogPo.setLogType(logType);
            jobLogPo.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobLogPo.setTaskTrackerIdentity(request.getIdentity());
            jobLogPo.setLevel(Level.INFO);
            jobLogPo.setLogTime(result.getTime());
            appContext.getJobLogger().log(jobLogPo);

            // 监控数据统计
            if (result.getAction() != null) {
                switch (result.getAction()) {
                    case EXECUTE_SUCCESS:
                        monitor.incExeSuccessNum();
                        break;
                    case EXECUTE_FAILED:
                        monitor.incExeFailedNum();
                        break;
                    case EXECUTE_LATER:
                        monitor.incExeLaterNum();
                        break;
                    case EXECUTE_EXCEPTION:
                        monitor.incExeExceptionNum();
                        break;
                }
            }
        }
        return null;
    }

}
