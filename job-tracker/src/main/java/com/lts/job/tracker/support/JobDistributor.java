package com.lts.job.tracker.support;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.RemotingSendException;
import com.lts.job.core.exception.RequestTimeoutException;
import com.lts.job.core.factory.NamedThreadFactory;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobPullRequest;
import com.lts.job.core.protocol.command.JobPushRequest;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.util.Holder;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.domain.TaskTrackerNode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         任务分发管理
 */
public class JobDistributor {

    private final Logger LOGGER = LoggerFactory.getLogger(JobDistributor.class);
    private JobTrackerApplication application;
    private final ExecutorService executor;

    public JobDistributor(JobTrackerApplication application) {
        this.application = application;
        executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 5
                , new NamedThreadFactory(JobDistributor.class.getSimpleName()));
    }

    public void distribute(final RemotingServerDelegate remotingServer, final JobPullRequest request) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    _sendJob(remotingServer, request);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 对 TaskTracker的每次请求进行处理
     * 分发任务等
     *
     * @param remotingServer
     * @param request
     */
    private void _sendJob(RemotingServerDelegate remotingServer, JobPullRequest request) {

        String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        // 更新TaskTracker的可用线程数
        application.getTaskTrackerManager().updateTaskTrackerAvailableThreads(nodeGroup, identity, request.getAvailableThreads(), request.getTimestamp());

        TaskTrackerNode taskTrackerNode = application.getTaskTrackerManager().getTaskTrackerNode(nodeGroup, identity);

        if (taskTrackerNode == null) {
            return;
        }

        int availableThreads = taskTrackerNode.getAvailableThread().get();

        while (availableThreads > 0) {
            // 推送任务
            int code = sendJob(remotingServer, taskTrackerNode);
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
    private int sendJob(RemotingServerDelegate remotingServer, TaskTrackerNode taskTrackerNode) {

        String nodeGroup = taskTrackerNode.getNodeGroup();
        String identity = taskTrackerNode.getIdentity();

        // 从mongo 中取一个可运行的job
        JobPo jobPo = application.getExecutableJobQueue().take(nodeGroup, identity);

        if (jobPo == null) {
            return NO_JOB;
        }

        JobPushRequest body = application.getCommandBodyWrapper().wrapper(new JobPushRequest());
        Job job = JobDomainConverter.convert(jobPo);
        body.setJob(job);
        RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.PUSH_JOB.code(), body);

        // 是否分发推送任务成功
        final Holder<Boolean> pushSuccess = new Holder<Boolean>(false);

        final CountDownLatch latch = new CountDownLatch(1);
        try {
            remotingServer.invokeAsync(taskTrackerNode.getChannel().getChannel(), commandRequest, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    try {
                        RemotingCommand responseCommand = responseFuture.getResponseCommand();
                        if (responseCommand == null) {
                            LOGGER.warn("Job push failed! response command is null!");
                            return;
                        }
                        if (responseCommand.getCode() == JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code()) {
                            pushSuccess.set(true);
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });

        } catch (RemotingSendException e) {
            LOGGER.error(e.getMessage(), e);
        }

        try {
            latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RequestTimeoutException(e);
        }

        if (!pushSuccess.get()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job push failed! nodeGroup=" + nodeGroup + ", identity=" + identity + ", job=" + job);
            }
            application.getExecutableJobQueue().resume(jobPo);
            return PUSH_FAILED;
        }
        application.getExecutingJobQueue().add(jobPo);
        application.getExecutableJobQueue().remove(job.getTaskTrackerNodeGroup(), job.getJobId());

        return PUSH_SUCCESS;
    }
}
