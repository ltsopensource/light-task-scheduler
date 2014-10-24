package com.lts.job.tracker.processor;

import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.JobResult;
import com.lts.job.common.domain.LogType;
import com.lts.job.common.exception.RemotingSendException;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.JobFinishedRequest;
import com.lts.job.common.protocol.command.JobPushRequest;
import com.lts.job.common.remoting.RemotingServerDelegate;
import com.lts.job.common.util.CollectionUtils;
import com.lts.job.remoting.exception.RemotingCommandException;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.tracker.domain.JobClientNode;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.common.support.JobDomainConverter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * TaskTracker 完成任务 的处理器
 */
public class JobFinishedProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("JobFinishedProcessor");

    public JobFinishedProcessor(RemotingServerDelegate remotingServer) {
        super(remotingServer);
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();

        List<JobResult> jobResults = requestBody.getJobResults();

        // 1. 检验参数
        if (CollectionUtils.isEmpty(jobResults)) {
            RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_PARAM_ERROR.code(), "JobFinishedRequest.jobResults can not be empty!");
        }

        if (requestBody.isReSend()) {
            JobLogger.log(jobResults, LogType.RESEND);
        } else {
            JobLogger.log(jobResults, LogType.FINISHED);
        }

        LOGGER.info("任务完成" + jobResults);

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
        List<JobResult> notNeedFeedbackList = new ArrayList<JobResult>();

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

        if (CollectionUtils.isNotEmpty(needFeedbackList)) {
            // 通知客户端
            notifyClient(needFeedbackList);
        }

        if (CollectionUtils.isNotEmpty(notNeedFeedbackList)) {
            // 不需要通知客户端的并且不是定时任务直接删除
            deleteJob(notNeedFeedbackList);
        }

        // 判断是否接受新任务
        if (requestBody.isReceiveNewJob()) {
            // 查看有没有其他可以执行的任务
            JobPo jobPo = jobRepository.getJobPo(requestBody.getNodeGroup(), requestBody.getIdentity());
            if (jobPo != null) {
                JobPushRequest jobPushRequest = new JobPushRequest();
                Job job = JobDomainConverter.convert(jobPo);
                jobPushRequest.setJob(job);
//                JobLogger.log(job, LogType.PUSH);
                LOGGER.info("发送任务" + job + "给 {" + requestBody.getNodeGroup() + " ," + requestBody.getIdentity() + "}");
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
        // 启动新的线程 去通知客户端
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // 发送给客户端
                    List<JobResult> failedJobResults = send(jobResults);

                    if (CollectionUtils.isNotEmpty(failedJobResults)) {
                        // 发送失败，将任务finished状态设置为true, 并且保存记录并重试
                        jobRepository.finishedJob(jobResults);

                    }
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
            }

            /**
             * 发送给客户端
             *
             * @param jobResults
             * @return
             */
            private List<JobResult> send(List<JobResult> jobResults) {
                // 单个 就不用 分组了
                if (jobResults.size() == 1) {

                    JobResult jobResult = jobResults.get(0);
                    if (!send0(jobResult.getJob().getNodeGroup(), jobResults)) {
                        // 如果没有完成就返回
                        return jobResults;
                    }
                    return null;

                } else if (jobResults.size() > 1) {

                    List<JobResult> failedJobResult = new ArrayList<JobResult>();

                    // 有多个要进行分组 (出现在 失败重发的时候)
                    Map<String/*nodeGroup*/, List<JobResult>> groupMap = new HashMap<String, List<JobResult>>();

                    for (JobResult jobResult : jobResults) {
                        List<JobResult> jobResultList = groupMap.get(jobResult.getJob().getNodeGroup());
                        if (jobResultList == null) {
                            jobResultList = new ArrayList<JobResult>();
                            groupMap.put(jobResult.getJob().getNodeGroup(), jobResultList);
                        }
                        jobResultList.add(jobResult);
                    }
                    for (Map.Entry<String, List<JobResult>> entry : groupMap.entrySet()) {


                        if (!send0(entry.getKey(), entry.getValue())) {
                            failedJobResult.addAll(entry.getValue());
                        }
                    }
                    return failedJobResult;
                }

                return null;
            }

            /**
             * 发送给客户端
             * 返回是否发送成功还是失败
             *
             * @param nodeGroup
             * @param jobResults
             * @return
             */
            private boolean send0(String nodeGroup, List<JobResult> jobResults) {
                // 得到 可用的客户端节点
                JobClientNode jobClientNode = JobClientManager.INSTANCE.getAvailableJobClient(nodeGroup);

                if (jobClientNode == null) {
                    return false;
                }

                JobFinishedRequest requestBody = new JobFinishedRequest();
                requestBody.setJobResults(jobResults);
                RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_FINISHED.code(), requestBody);
                try {
                    RemotingCommand commandResponse = remotingServer.invokeSync(jobClientNode.getChannel().getChannel(), commandRequest);

                    if (commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                        deleteJob(jobResults);
                        return true;
                    }
                } catch (RemotingSendException e) {
                    LOGGER.error("通知客户端失败!", e);
                } catch (RemotingCommandFieldCheckException e) {
                    LOGGER.error("通知客户端失败!", e);
                }
                return false;
            }

        }).start();
    }

    private void deleteJob(List<JobResult> jobResults) {
        for (JobResult jobResult : jobResults) {
            jobRepository.delJob(jobResult.getJob().getJobId());
        }
    }
}
