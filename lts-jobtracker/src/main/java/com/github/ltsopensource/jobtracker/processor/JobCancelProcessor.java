package com.github.ltsopensource.jobtracker.processor;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobCancelRequest;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.exception.RemotingCommandException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 11/7/15.
 */
public class JobCancelProcessor extends AbstractRemotingProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(JobCancelProcessor.class);

    public JobCancelProcessor(JobTrackerAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        JobCancelRequest jobCancelRequest = request.getBody();

        String taskId = jobCancelRequest.getTaskId();
        String taskTrackerNodeGroup = jobCancelRequest.getTaskTrackerNodeGroup();
        JobPo jobPo = appContext.getCronJobQueue().getJob(taskTrackerNodeGroup, taskId);
        if (jobPo == null) {
            jobPo = appContext.getRepeatJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }
        if (jobPo == null) {
            jobPo = appContext.getExecutableJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }
        if (jobPo == null) {
            jobPo = appContext.getSuspendJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }

        if (jobPo != null) {
            // 队列都remove下吧
            appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            if (jobPo.isCron()) {
                appContext.getCronJobQueue().remove(jobPo.getJobId());
            } else if (jobPo.isRepeatable()) {
                appContext.getRepeatJobQueue().remove(jobPo.getJobId());
            }
            appContext.getSuspendJobQueue().remove(jobPo.getJobId());

            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.DEL);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setLevel(Level.INFO);
            appContext.getJobLogger().log(jobLogPo);

            LOGGER.info("Cancel Job success , jobId={}, taskId={}, taskTrackerNodeGroup={}", jobPo.getJobId(), taskId, taskTrackerNodeGroup);
            return RemotingCommand.createResponseCommand(JobProtos
                    .ResponseCode.JOB_CANCEL_SUCCESS.code());
        }

        return RemotingCommand.createResponseCommand(JobProtos
                .ResponseCode.JOB_CANCEL_FAILED.code(), "Job maybe running");
    }
}
