package com.github.ltsopensource.jobtracker.complete.biz;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.JobRunResult;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.command.JobCompletedRequest;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.monitor.JobTrackerMStatReporter;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;

import java.util.List;

/**
 * 任务数据统计 Chain
 *
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public class JobStatBiz implements JobCompletedBiz {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobStatBiz.class);

    private JobTrackerAppContext appContext;
    private JobTrackerMStatReporter stat;

    public JobStatBiz(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.stat = (JobTrackerMStatReporter) appContext.getMStatReporter();

    }

    @Override
    public RemotingCommand doBiz(JobCompletedRequest request) {

        List<JobRunResult> results = request.getJobRunResults();

        if (CollectionUtils.isEmpty(results)) {
            return RemotingCommand.createResponseCommand(RemotingProtos
                            .ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobResults can not be empty!");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Job execute completed : {}", results);
        }

        LogType logType = request.isReSend() ? LogType.RESEND : LogType.FINISHED;

        for (JobRunResult result : results) {

            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(result.getJobMeta());
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
                        stat.incExeSuccessNum();
                        break;
                    case EXECUTE_FAILED:
                        stat.incExeFailedNum();
                        break;
                    case EXECUTE_LATER:
                        stat.incExeLaterNum();
                        break;
                    case EXECUTE_EXCEPTION:
                        stat.incExeExceptionNum();
                        break;
                }
            }
        }
        return null;
    }

}
