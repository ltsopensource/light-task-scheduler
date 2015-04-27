package com.lts.job.tracker.processor;

import com.lts.job.core.constant.Level;
import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.domain.LogType;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.protocol.command.JobPushRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.support.CronExpression;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.logger.domain.JobLogPo;
import com.lts.job.tracker.queue.JobFeedbackPo;
import com.lts.job.tracker.queue.JobFeedbackQueue;
import com.lts.job.tracker.queue.JobPo;
import com.lts.job.tracker.queue.JobQueue;
import com.lts.job.tracker.support.ClientNotifier;
import com.lts.job.tracker.support.ClientNotifyHandler;
import com.lts.job.tracker.support.JobDomainConverter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
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
    private CommandBodyWrapper commandBodyWrapper;
    private JobLogger jobLogger;
    private JobQueue jobQueue;
    private JobFeedbackQueue jobFeedbackQueue;

    public JobFinishedProcessor(RemotingServerDelegate remotingServer, JobTrackerApplication application) {
        super(remotingServer, application);
        this.jobLogger = application.getJobLogger();
        this.jobQueue = application.getJobQueue();
        this.jobFeedbackQueue = application.getJobFeedbackQueue();
        this.commandBodyWrapper = application.getCommandBodyWrapper();
        this.clientNotifier = new ClientNotifier(application, new ClientNotifyHandler() {
            @Override
            public void handleSuccess(List<JobResult> jobResults) {
                finishedJob(jobResults);
            }

            @Override
            public void handleFailed(List<JobResult> jobResults) {
                if (CollectionUtils.isNotEmpty(jobResults)) {
                    List<JobFeedbackPo> jobFeedbackPos =
                            new ArrayList<JobFeedbackPo>(jobResults.size());

                    for (JobResult jobResult : jobResults) {
                        JobFeedbackPo jobFeedbackPo =
                                JobDomainConverter.convert(jobResult);
                        jobFeedbackPos.add(jobFeedbackPo);
                    }
                    // 2. 失败的存储在反馈队列
                    jobFeedbackQueue.add(jobFeedbackPos);
                    // 3. 完成任务 
                    finishedJob(jobResults);
                }
            }
        });
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();

        List<JobResult> jobResults = requestBody.getJobResults();

        // 1. 检验参数
        if (CollectionUtils.isEmpty(jobResults)) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobFinishedRequest.jobResults can not be empty!");
        }

        if (requestBody.isReSend()) {
            log(requestBody.getIdentity(), jobResults, LogType.RESEND);
        } else {
            log(requestBody.getIdentity(), jobResults, LogType.FINISHED);
        }

        LOGGER.info("执行任务完成: {}", jobResults);

        return finishJob(requestBody, jobResults);
    }

    /**
     * 记录日志
     *
     * @param jobResults
     * @param logType
     */
    private void log(String taskTrackerIdentity, List<JobResult> jobResults, LogType logType) {
        try {
            for (JobResult jobResult : jobResults) {
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobResult.getJob());
                jobLogPo.setMsg(jobResult.getMsg());
                jobLogPo.setLogType(logType);
                jobLogPo.setSuccess(jobResult.isSuccess());
                jobLogPo.setTaskTrackerIdentity(taskTrackerIdentity);
                jobLogPo.setLevel(Level.INFO);
                jobLogger.log(jobLogPo);
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
    private RemotingCommand finishJob(JobFinishedRequest requestBody, List<JobResult> jobResults) {

        // 过滤出来需要通知客户端的
        List<JobResult> needFeedbackList = null;
        // 不需要反馈的
        List<JobResult> notNeedFeedbackList = null;

        for (JobResult jobResult : jobResults) {
            if (jobResult.getJob().isNeedFeedback()) {
                if (needFeedbackList == null) {
                    needFeedbackList = new ArrayList<JobResult>();
                }
                needFeedbackList.add(jobResult);
            } else {
                if (notNeedFeedbackList == null) {
                    notNeedFeedbackList = new ArrayList<JobResult>();
                }
                notNeedFeedbackList.add(jobResult);
            }
        }

        // 通知客户端
        notifyClient(needFeedbackList);

        // 不需要通知客户端的并且不是定时任务直接删除
        finishedJob(notNeedFeedbackList);

        // 判断是否接受新任务
        if (requestBody.isReceiveNewJob()) {
            // 查看有没有其他可以执行的任务
            JobPo jobPo = jobQueue.take(requestBody.getNodeGroup(), requestBody.getIdentity());
            if (jobPo != null) {
                JobPushRequest jobPushRequest = commandBodyWrapper.wrapper(new JobPushRequest());
                Job job = JobDomainConverter.convert(jobPo);
                jobPushRequest.setJob(job);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("发送任务{}给 {} {} ", job, requestBody.getNodeGroup(), requestBody.getIdentity());
                }
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
     * @param jobResults
     */
    private void notifyClient(final List<JobResult> jobResults) {

        if (CollectionUtils.isEmpty(jobResults)) {
            return;
        }
        // 1.发送给客户端
        clientNotifier.send(jobResults);
    }

    private void finishedJob(List<JobResult> jobResults) {
        if (CollectionUtils.isEmpty(jobResults)) {
            return;
        }
        for (JobResult jobResult : jobResults) {
            Job job = jobResult.getJob();
            if (!job.isSchedule()) {
                jobQueue.remove(job.getJobId());
            } else {
                try {
                    CronExpression cronExpression = new CronExpression(job.getCronExpression());
                    Date nextTriggerTime = cronExpression.getTimeAfter(new Date());
                    if (nextTriggerTime == null) {
                        // 执行完成了，要删除
                        jobQueue.remove(job.getJobId());
                    } else {
                        // 表示下次还要执行
                        jobQueue.updateScheduleTriggerTime(job.getJobId(), nextTriggerTime.getTime());
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
