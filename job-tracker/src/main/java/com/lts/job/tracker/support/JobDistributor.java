package com.lts.job.tracker.support;

import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.RemotingSendException;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobPullRequest;
import com.lts.job.core.protocol.command.JobPushRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.domain.TaskTrackerNode;
import com.lts.job.tracker.queue.JobPo;
import com.lts.job.tracker.queue.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         任务分发管理
 */
public class JobDistributor {

    private final Logger LOGGER = LoggerFactory.getLogger(JobDistributor.class);
    private JobQueue jobQueue;
    private TaskTrackerManager taskTrackerManager;
    private CommandBodyWrapper commandBodyWrapper;

    public JobDistributor(JobTrackerApplication application) {
        this.jobQueue = application.getJobQueue();
        this.taskTrackerManager = application.getTaskTrackerManager();
        this.commandBodyWrapper = application.getCommandBodyWrapper();
    }

    /**
     * 对 TaskTracker的每次请求进行处理
     * 分发任务等
     *
     * @param remotingServer
     * @param request
     */
    public void pushJob(RemotingServerDelegate remotingServer, JobPullRequest request) {

        String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        // 更新TaskTracker的可用线程数
        taskTrackerManager.updateTaskTrackerAvailableThreads(nodeGroup, identity, request.getAvailableThreads(), request.getTimestamp());

        TaskTrackerNode taskTrackerNode = taskTrackerManager.getTaskTrackerNode(nodeGroup, identity);

        if (taskTrackerNode == null) {
            return;
        }

        int availableThreads = taskTrackerNode.getAvailableThread().get();

        while (availableThreads > 0) {
            // 推送任务
            int code = pushJob(remotingServer, taskTrackerNode);
            if (code == NO_JOB) {
                // 没有可以执行的任务, 直接停止
                break;
            }
            if (code == PUSH_FAILED) {
                break;
            }
            availableThreads = taskTrackerNode.getAvailableThread().get();
        }
    }

    // 没有任务可执行
    private final int NO_JOB = 1;
    // 推送成功
    private final int PUSH_SUCCESS = 2;
    // 推送失败
    private final int PUSH_FAILED = 3;


    /**
     * 是否推送成功
     *
     * @param remotingServer
     * @param taskTrackerNode
     * @return
     */
    private int pushJob(RemotingServerDelegate remotingServer, TaskTrackerNode taskTrackerNode) {

        String nodeGroup = taskTrackerNode.getNodeGroup();
        String identity = taskTrackerNode.getIdentity();

        // 从mongo 中取一个可运行的job
        JobPo jobPo = jobQueue.take(nodeGroup, identity);

        if (jobPo == null) {
            return NO_JOB;
        }

        JobPushRequest body = commandBodyWrapper.wrapper(new JobPushRequest());
        Job job = JobDomainConverter.convert(jobPo);
        body.setJob(job);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.PUSH_JOB.code(), body);

        // 是否分发推送任务成功
        final boolean[] pushSuccess = {false};

        final CountDownLatch latch = new CountDownLatch(1);
        try {
            remotingServer.invokeAsync(taskTrackerNode.getChannel().getChannel(), commandRequest, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand responseCommand = responseFuture.getResponseCommand();
                        if (responseCommand == null) {
                            LOGGER.warn("job push failed! response command is null!");
                            return;
                        }
                        if (responseCommand.getCode() == JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code()) {
                            pushSuccess[0] = true;
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });

        } catch (RemotingSendException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (RemotingCommandFieldCheckException e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!pushSuccess[0]) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("nodeGroup=" + nodeGroup + ", identity=" + identity + ", 任务没有推送成功, job=" + job);
            }
            jobQueue.resume(jobPo);
            return PUSH_FAILED;
        }

        return PUSH_SUCCESS;
    }
}
