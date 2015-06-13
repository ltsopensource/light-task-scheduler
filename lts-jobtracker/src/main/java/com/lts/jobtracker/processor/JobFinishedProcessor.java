package com.lts.jobtracker.processor;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.constant.Level;
import com.lts.core.domain.TaskTrackerJobResult;
import com.lts.core.domain.JobWrapper;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.command.TtJobFinishedRequest;
import com.lts.core.protocol.command.JobPushRequest;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.queue.domain.JobFeedbackPo;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.lts.remoting.exception.RemotingCommandException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.support.ClientNotifier;
import com.lts.jobtracker.support.ClientNotifyHandler;
import com.lts.jobtracker.support.CronExpressionUtils;
import com.lts.jobtracker.support.JobDomainConverter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         TaskTracker 完成任务 的处理器
 */
public class JobFinishedProcessor extends AbstractProcessor {

    private ClientNotifier clientNotifier;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class.getSimpleName());

    public JobFinishedProcessor(RemotingServerDelegate remotingServer, final JobTrackerApplication application) {
        super(remotingServer, application);
        this.clientNotifier = new ClientNotifier(application, new ClientNotifyHandler<TaskTrackerJobResult>() {
            @Override
            public void handleSuccess(List<TaskTrackerJobResult> taskTrackerJobResults) {
                finishedJob(taskTrackerJobResults);
            }

            @Override
            public void handleFailed(List<TaskTrackerJobResult> taskTrackerJobResults) {
                if (CollectionUtils.isNotEmpty(taskTrackerJobResults)) {
                    List<JobFeedbackPo> jobFeedbackPos =
                            new ArrayList<JobFeedbackPo>(taskTrackerJobResults.size());

                    for (TaskTrackerJobResult taskTrackerJobResult : taskTrackerJobResults) {
                        JobFeedbackPo jobFeedbackPo =
                                JobDomainConverter.convert(taskTrackerJobResult);
                        jobFeedbackPos.add(jobFeedbackPo);
                    }
                    // 2. 失败的存储在反馈队列
                    application.getJobFeedbackQueue().add(jobFeedbackPos);
                    // 3. 完成任务 
                    finishedJob(taskTrackerJobResults);
                }
            }
        });
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        TtJobFinishedRequest requestBody = request.getBody();

        List<TaskTrackerJobResult> taskTrackerJobResults = requestBody.getTaskTrackerJobResults();

        // 1. 检验参数
        if (CollectionUtils.isEmpty(taskTrackerJobResults)) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobFinishedRequest.jobResults can not be empty!");
        }

        if (requestBody.isReSend()) {
            log(requestBody.getIdentity(), taskTrackerJobResults, LogType.RESEND);
        } else {
            log(requestBody.getIdentity(), taskTrackerJobResults, LogType.FINISHED);
        }

        LOGGER.info("job exec finished : {}", taskTrackerJobResults);

        return finishJob(requestBody, taskTrackerJobResults);
    }

    /**
     * 记录日志
     *
     * @param taskTrackerJobResults
     * @param logType
     */
    private void log(String taskTrackerIdentity, List<TaskTrackerJobResult> taskTrackerJobResults, LogType logType) {
        try {
            for (TaskTrackerJobResult taskTrackerJobResult : taskTrackerJobResults) {
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(taskTrackerJobResult.getJobWrapper());
                jobLogPo.setMsg(taskTrackerJobResult.getMsg());
                jobLogPo.setLogType(logType);
                jobLogPo.setSuccess(taskTrackerJobResult.isSuccess());
                jobLogPo.setTaskTrackerIdentity(taskTrackerIdentity);
                jobLogPo.setLevel(Level.INFO);
                application.getJobLogger().log(jobLogPo);
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    /**
     * taskTracker 完成非定时任务
     *
     * @param requestBody
     * @return
     */
    private RemotingCommand finishJob(TtJobFinishedRequest requestBody, List<TaskTrackerJobResult> taskTrackerJobResults) {

        // 过滤出来需要通知客户端的
        List<TaskTrackerJobResult> needFeedbackList = null;
        // 不需要反馈的
        List<TaskTrackerJobResult> notNeedFeedbackList = null;

        for (TaskTrackerJobResult taskTrackerJobResult : taskTrackerJobResults) {
            if (taskTrackerJobResult.getJobWrapper().getJob().isNeedFeedback()) {
                if (needFeedbackList == null) {
                    needFeedbackList = new ArrayList<TaskTrackerJobResult>();
                }
                needFeedbackList.add(taskTrackerJobResult);
            } else {
                if (notNeedFeedbackList == null) {
                    notNeedFeedbackList = new ArrayList<TaskTrackerJobResult>();
                }
                notNeedFeedbackList.add(taskTrackerJobResult);
            }
        }

        // 通知客户端
        notifyClient(needFeedbackList);

        // 不需要通知客户端的并且不是定时任务直接删除
        finishedJob(notNeedFeedbackList);

        // 判断是否接受新任务
        if (requestBody.isReceiveNewJob()) {
            // 查看有没有其他可以执行的任务
            JobPo jobPo = application.getExecutableJobQueue().take(requestBody.getNodeGroup(), requestBody.getIdentity());
            if (jobPo != null) {
                JobPushRequest jobPushRequest = application.getCommandBodyWrapper().wrapper(new JobPushRequest());
                jobPushRequest.setJobWrapper(JobDomainConverter.convert(jobPo));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("send job {} to {} {} ", jobPo, requestBody.getNodeGroup(), requestBody.getIdentity());
                }
                try {
                    application.getExecutingJobQueue().add(jobPo);
                } catch (DuplicateJobException e) {
                    // ignore
                }
                application.getExecutableJobQueue().remove(jobPo.getTaskTrackerNodeGroup(), jobPo.getJobId());

                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
                jobLogPo.setSuccess(true);
                jobLogPo.setLogType(LogType.SENT);
                jobLogPo.setLevel(Level.INFO);
                application.getJobLogger().log(jobLogPo);

                // 返回 新的任务
                return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), "receive msg success and has new job!", jobPushRequest);
            }
        }
        // 返回给 任务执行端
        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), "receive msg success!");
    }

    /**
     * 启动新的线程去通知客户端
     *
     * @param taskTrackerJobResults
     */
    private void notifyClient(final List<TaskTrackerJobResult> taskTrackerJobResults) {

        if (CollectionUtils.isEmpty(taskTrackerJobResults)) {
            return;
        }
        // 1.发送给客户端
        clientNotifier.send(taskTrackerJobResults);
    }

    private void finishedJob(List<TaskTrackerJobResult> taskTrackerJobResults) {
        if (CollectionUtils.isEmpty(taskTrackerJobResults)) {
            return;
        }
        for (TaskTrackerJobResult taskTrackerJobResult : taskTrackerJobResults) {
            JobWrapper jobWrapper = taskTrackerJobResult.getJobWrapper();
            // 移除
            application.getExecutingJobQueue().remove(jobWrapper.getJobId());

            if (jobWrapper.getJob().isSchedule()) {

                JobPo cronJobPo = application.getCronJobQueue().finish(jobWrapper.getJobId());
                if (cronJobPo == null) {
                    // 可能任务队列中改条记录被删除了
                    return;
                }
                Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronJobPo.getCronExpression());
                if (nextTriggerTime == null) {
                    application.getCronJobQueue().remove(jobWrapper.getJobId());
                } else {
                    // 表示下次还要执行
                    try {
                        cronJobPo.setTriggerTime(nextTriggerTime.getTime());
                        cronJobPo.setGmtModified(System.currentTimeMillis());
                        application.getExecutableJobQueue().add(cronJobPo);
                    } catch (DuplicateJobException e) {
                        LOGGER.warn("this cron job is duplicate !", e);
                    }
                }
            }
        }
    }
}
