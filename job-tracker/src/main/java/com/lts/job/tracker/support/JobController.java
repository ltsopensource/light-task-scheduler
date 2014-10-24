package com.lts.job.tracker.support;

import com.lts.job.common.domain.Job;
import com.lts.job.common.domain.LogType;
import com.lts.job.common.exception.RemotingSendException;
import com.lts.job.common.protocol.JobProtos;
import com.lts.job.common.protocol.command.AbstractCommandBody;
import com.lts.job.common.protocol.command.JobPushRequest;
import com.lts.job.common.remoting.RemotingServerDelegate;
import com.lts.job.common.support.JobDomainConverter;
import com.lts.job.common.support.SingletonBeanContext;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.TaskTrackerNode;
import com.lts.job.common.repository.JobMongoRepository;
import com.lts.job.common.repository.po.JobPo;
import com.lts.job.tracker.logger.JobLogger;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 * 任务分发管理
 */
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);
    private static JobMongoRepository jobRepository;

    static {
        jobRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
    }

    /**
     * 对 TaskTracker的每次请求进行处理
     * 分发任务等
     *
     * @param remotingServer
     * @param ctx
     * @param request
     */
    public static void pushJob(RemotingServerDelegate remotingServer, ChannelHandlerContext ctx, RemotingCommand request) {

        AbstractCommandBody requestBody = request.getBody();
        String nodeGroup = requestBody.getNodeGroup();

        // 推送失败的节点 要过滤掉
        HashSet<TaskTrackerNode> failedNodes = new HashSet<TaskTrackerNode>();

        while (true) {

            TaskTrackerNode taskTrackerNode = TaskTrackerManager.INSTANCE.getIdleTaskTrackerNode(nodeGroup, failedNodes);

            if (taskTrackerNode != null) {
                // 推送任务
                int code = pushJob(remotingServer, taskTrackerNode);

                if (code == NO_JOB) {
                    // 没有可以执行的任务, 直接停止
                    break;
                }
                if (code == PUSH_FAILED) {
                    failedNodes.add(taskTrackerNode);
                }
            } else {
                break;
            }
        }
    }

    // 没有任务可执行
    private static final int NO_JOB = 1;
    // 推送成功
    private static final int PUSH_SUCCESS = 2;
    // 推送失败
    private static final int PUSH_FAILED = 3;


    /**
     * 是否推送成功
     *
     * @param remotingServer
     * @param taskTrackerNode
     * @return
     */
    private static int pushJob(RemotingServerDelegate remotingServer, TaskTrackerNode taskTrackerNode) {

        String nodeGroup = taskTrackerNode.getNodeGroup();
        String identity = taskTrackerNode.getIdentity();

        // 从mongo 中取一个可运行的job
        JobPo jobPo = jobRepository.getJobPo(nodeGroup, identity);

        if (jobPo == null) {
            return NO_JOB;
        }

        if (jobPo != null) {

            JobPushRequest body = new JobPushRequest();
            Job job = JobDomainConverter.convert(jobPo);
            body.setJob(job);
            RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.PUSH_JOB.code(), body);

            // 是否分发推送任务成功
            boolean pushSuccess = false;

            try {
                RemotingCommand commandResponse = remotingServer.invokeSync(taskTrackerNode.getChannel().getChannel(), commandRequest);

                if (commandResponse.getCode() == JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code()) {
//                    JobLogger.log(job, LogType.PUSH);
                    pushSuccess = true;
                }

            } catch (RemotingSendException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (RemotingCommandFieldCheckException e) {
                LOGGER.error(e.getMessage(), e);
            }

            if (!pushSuccess) {
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("nodeGroup=" + nodeGroup + ", identity=" + identity + ", 任务没有推送成功, job=" + job);
                }
                jobPo.setRemark("identity=" + identity + ", 任务没有推送成功");
                jobRepository.setJobRunnable(jobPo);
                return PUSH_FAILED;
            }
        }

        return PUSH_SUCCESS;
    }
}
