package com.lts.job.tracker.processor;

import com.lts.job.core.domain.Job;
import com.lts.job.core.domain.JobResult;
import com.lts.job.core.domain.LogType;
import com.lts.job.core.protocol.command.JobFinishedRequest;
import com.lts.job.core.protocol.command.JobPushRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.repository.JobFeedbackQueueMongoRepository;
import com.lts.job.core.repository.po.JobFeedbackQueuePo;
import com.lts.job.core.support.CronExpression;
import com.lts.job.core.support.SingletonBeanContext;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.core.repository.po.JobPo;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.support.ClientNotifier;
import com.lts.job.core.support.JobDomainConverter;
import com.lts.job.tracker.support.ClientNotifyHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         TaskTracker 完成任务 的处理器
 */
public class JobFinishedProcessor extends AbstractProcessor {

    private ClientNotifier clientNotifier;
    private JobFeedbackQueueMongoRepository jobFeedbackQueueMongoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFinishedProcessor.class.getSimpleName());

    public JobFinishedProcessor(RemotingServerDelegate remotingServer) {
        super(remotingServer);
        this.jobFeedbackQueueMongoRepository = SingletonBeanContext.getBean(JobFeedbackQueueMongoRepository.class);
        this.clientNotifier = new ClientNotifier(remotingServer, new ClientNotifyHandler() {
            @Override
            public void handleSuccess(List<JobResult> jobResults) {
                finishedJob(jobResults);
            }

            @Override
            public void handleFailed(List<JobResult> jobResults) {
                if (CollectionUtils.isNotEmpty(jobResults)) {
                    List<JobFeedbackQueuePo> jobFeedbackQueuePos = new ArrayList<JobFeedbackQueuePo>(jobResults.size());

                    for (JobResult jobResult : jobResults) {
                        JobFeedbackQueuePo jobFeedbackQueuePo = new JobFeedbackQueuePo();
                        try {
                            BeanUtils.copyProperties(jobFeedbackQueuePo, jobResult);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        jobFeedbackQueuePo.setId(UUID.randomUUID().toString());
                        jobFeedbackQueuePo.setGmtCreated(System.currentTimeMillis());
                        jobFeedbackQueuePos.add(jobFeedbackQueuePo);
                    }
                    // 2. 失败的存储在反馈队列
                    jobFeedbackQueueMongoRepository.save(jobFeedbackQueuePos);
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
            RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "JobFinishedRequest.jobResults can not be empty!");
        }

        if (requestBody.isReSend()) {
            JobLogger.log(jobResults, LogType.RESEND);
        } else {
            JobLogger.log(jobResults, LogType.FINISHED);
        }

        LOGGER.info("任务完成: {}", jobResults);

        return finishJob(requestBody, jobResults);
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
            JobPo jobPo = jobRepository.getJobPo(requestBody.getNodeGroup(), requestBody.getIdentity());
            if (jobPo != null) {
                JobPushRequest jobPushRequest = new JobPushRequest();
                Job job = JobDomainConverter.convert(jobPo);
                jobPushRequest.setJob(job);
                LOGGER.info("发送任务{}给 {} {} ", job, requestBody.getNodeGroup(), requestBody.getIdentity());
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
                jobRepository.delJob(jobResult.getJob().getJobId());
            } else {
                try {
                    CronExpression cronExpression = new CronExpression(job.getCronExpression());
                    Date nextTriggerTime = cronExpression.getTimeAfter(new Date());
                    if (nextTriggerTime == null) {
                        // 执行完成了，要删除
                        jobRepository.delJob(jobResult.getJob().getJobId());
                    } else {
                        // 表示下次还要执行
                        jobRepository.updateTriggerTime(jobResult, nextTriggerTime);
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
