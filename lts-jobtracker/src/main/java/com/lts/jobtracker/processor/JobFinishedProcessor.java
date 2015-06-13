package com.lts.jobtracker.processor;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.core.constant.Constants;
import com.lts.core.constant.Level;
import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class);
    // 任务的最大重试次数
    private final Integer maxRetryTimes;

    public JobFinishedProcessor(RemotingServerDelegate remotingServer, final JobTrackerApplication application) {
        super(remotingServer, application);
        this.maxRetryTimes = application.getConfig().getParameter(Constants.JOB_MAX_RETRY_TIMES,
                Constants.DEFAULT_JOB_MAX_RETRY_TIMES);

        this.clientNotifier = new ClientNotifier(application, new ClientNotifyHandler<TaskTrackerJobResult>() {
            @Override
            public void handleSuccess(List<TaskTrackerJobResult> results) {
                finishProcess(results);
            }

            @Override
            public void handleFailed(List<TaskTrackerJobResult> results) {
                if (CollectionUtils.isNotEmpty(results)) {
                    List<JobFeedbackPo> jobFeedbackPos =
                            new ArrayList<JobFeedbackPo>(results.size());

                    for (TaskTrackerJobResult result : results) {
                        JobFeedbackPo jobFeedbackPo =
                                JobDomainConverter.convert(result);
                        jobFeedbackPos.add(jobFeedbackPo);
                    }
                    // 2. 失败的存储在反馈队列
                    application.getJobFeedbackQueue().add(jobFeedbackPos);
                    // 3. 完成任务 
                    finishProcess(results);
                }
            }
        });
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {

        TtJobFinishedRequest requestBody = request.getBody();

        List<TaskTrackerJobResult> results = requestBody.getTaskTrackerJobResults();

        // 1. check params
        if (CollectionUtils.isEmpty(results)) {
            return RemotingCommand.createResponseCommand(RemotingProtos
                            .ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobResults can not be empty!");
        }

        // 2. log info
        log(requestBody.isReSend(), requestBody.getIdentity(), results);

        LOGGER.info("Job exec finished : {}", results);

        // 3. process
        return process(requestBody.isReceiveNewJob(), requestBody.getNodeGroup(),
                requestBody.getIdentity(), results);
    }

    /**
     * 记录日志
     */
    private void log(boolean resend, String taskTrackerIdentity,
                     List<TaskTrackerJobResult> results) {

        LogType logType = resend ? LogType.RESEND : LogType.FINISHED;

        for (TaskTrackerJobResult result : results) {

            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(result.getJobWrapper());
            jobLogPo.setMsg(result.getMsg());
            jobLogPo.setLogType(logType);
            jobLogPo.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobLogPo.setTaskTrackerIdentity(taskTrackerIdentity);
            jobLogPo.setLevel(Level.INFO);
            jobLogPo.setLogTime(result.getTime());
            application.getJobLogger().log(jobLogPo);
        }
    }

    /**
     * 处理
     */
    private RemotingCommand process(boolean receiveNewJob,
                                    String taskTrackerNodeGroup,
                                    String taskTrackerIdentity,
                                    List<TaskTrackerJobResult> results) {

        if (CollectionUtils.sizeOf(results) == 1) {
            singleResultsProcess(results);
        } else {
            multiResultsProcess(results);
        }

        // 判断是否接受新任务
        if (receiveNewJob) {
            // 查看有没有其他可以执行的任务
            JobPushRequest jobPushRequest = getNewJob(taskTrackerNodeGroup, taskTrackerIdentity);
            // 返回 新的任务
            return RemotingCommand.createResponseCommand(RemotingProtos
                    .ResponseCode.SUCCESS.code(), jobPushRequest);
        }

        // 返回给 任务执行端
        return RemotingCommand.createResponseCommand(RemotingProtos
                .ResponseCode.SUCCESS.code());
    }

    private void singleResultsProcess(List<TaskTrackerJobResult> results) {
        TaskTrackerJobResult result = results.get(0);

        if (!needRetry(result)) {
            // 这种情况下，如果要反馈客户端的，直接反馈客户端，不进行重试
            if (result.getJobWrapper().getJob().isNeedFeedback()) {
                clientNotifier.send(results);
            } else {
                finishProcess(results);
            }
        } else {
            // 需要retry
            retryProcess(results);
        }
    }

    /**
     * 判断任务是否需要加入重试队列
     */
    private boolean needRetry(TaskTrackerJobResult result) {
        // TODO 是否需要加个时间过滤 result.getTime()

        // 判断重试次数
        Job job = result.getJobWrapper().getJob();
        Integer retryTimes = job.getRetryTimes();
        if (retryTimes >= maxRetryTimes) {
            // 重试次数过多
            return false;
        }
        // 判断类型
        return !(Action.EXECUTE_SUCCESS.equals(result.getAction())
                || Action.EXECUTE_FAILED.equals(result.getAction()));
    }

    /**
     * 这里情况一般是发送失败，重新发送的
     */
    private void multiResultsProcess(List<TaskTrackerJobResult> results) {

        List<TaskTrackerJobResult> retryResults = null;

        // 过滤出来需要通知客户端的
        List<TaskTrackerJobResult> feedbackResults = null;
        // 不需要反馈的
        List<TaskTrackerJobResult> finishResults = null;

        for (TaskTrackerJobResult result : results) {

            if (needRetry(result)) {
                // 需要加入到重试队列的
                if (retryResults == null) {
                    retryResults = new ArrayList<TaskTrackerJobResult>();
                }
                retryResults.add(result);
            } else if (result.getJobWrapper().getJob().isNeedFeedback()) {
                // 需要反馈给客户端
                if (feedbackResults == null) {
                    feedbackResults = new ArrayList<TaskTrackerJobResult>();
                }
                feedbackResults.add(result);
            } else {
                // 不用反馈客户端，也不用重试，直接完成处理
                if (finishResults == null) {
                    finishResults = new ArrayList<TaskTrackerJobResult>();
                }
                finishResults.add(result);
            }
        }

        // 通知客户端
        clientNotifier.send(feedbackResults);

        // 完成任务
        finishProcess(finishResults);

        // 将任务加入到重试队列
        retryProcess(retryResults);
    }

    /**
     * 获取新任务去执行
     */
    private JobPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        JobPo jobPo = application.getExecutableJobQueue().take(taskTrackerNodeGroup, taskTrackerIdentity);
        if (jobPo == null) {
            return null;
        }
        JobPushRequest jobPushRequest = application.getCommandBodyWrapper().wrapper(new JobPushRequest());
        jobPushRequest.setJobWrapper(JobDomainConverter.convert(jobPo));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Send job {} to {} {} ", jobPo, taskTrackerNodeGroup, taskTrackerIdentity);
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
        jobLogPo.setLogTime(DateUtils.currentTimeMillis());
        application.getJobLogger().log(jobLogPo);
        return jobPushRequest;
    }

    /**
     * 完成任务
     */
    private void finishProcess(List<TaskTrackerJobResult> results) {

        if (CollectionUtils.isEmpty(results)) {
            return;
        }

        for (TaskTrackerJobResult result : results) {

            JobWrapper jobWrapper = result.getJobWrapper();
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
                        cronJobPo.setIsRunning(false);
                        cronJobPo.setTriggerTime(nextTriggerTime.getTime());
                        cronJobPo.setGmtModified(System.currentTimeMillis());
                        application.getExecutableJobQueue().add(cronJobPo);
                    } catch (DuplicateJobException e) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Cron job :{}  is duplicate !", cronJobPo, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 将任务加入重试队列
     */
    private void retryProcess(List<TaskTrackerJobResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        for (TaskTrackerJobResult result : results) {
            JobWrapper jobWrapper = result.getJobWrapper();
            // 1. 加入到重试队列
            JobPo jobPo = application.getExecutingJobQueue().get(jobWrapper.getJobId());
            if (jobPo != null) {
                // 重试次数+1
                jobPo.setRetryTimes((jobPo.getRetryTimes() == null ? 0 : jobPo.getRetryTimes()) + 1);
                Long nextRetryTriggerTime = DateUtils.addMinute(new Date(), jobPo.getRetryTimes()).getTime();
                // 延迟重试时间就等于重试次数(分钟)
                jobPo.setTriggerTime(nextRetryTriggerTime);

                boolean needAdd = true;

                if (jobPo.isSchedule()) {
                    // 如果是 cron Job, 判断任务下一次执行时间和重试时间的比较
                    JobPo cronJobPo = application.getCronJobQueue().finish(jobWrapper.getJobId());
                    if (cronJobPo != null) {
                        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronJobPo.getCronExpression());
                        if (nextTriggerTime != null && nextTriggerTime.getTime() < nextRetryTriggerTime) {
                            // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                            try {
                                cronJobPo.setIsRunning(false);
                                cronJobPo.setTriggerTime(nextTriggerTime.getTime());
                                cronJobPo.setGmtModified(System.currentTimeMillis());
                                application.getExecutableJobQueue().add(cronJobPo);
                            } catch (DuplicateJobException e) {
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn("Cron job :{}  is duplicate !", cronJobPo, e);
                                }
                            }
                            needAdd = false;
                        }
                    }
                }
                if (needAdd) {
                    // 加入到队列, 重试
                    jobPo.setIsRunning(false);
                    application.getExecutableJobQueue().add(jobPo);
                }
                // 从正在执行的队列中移除
                application.getExecutingJobQueue().remove(jobPo.getJobId());
            }
        }
    }
}
