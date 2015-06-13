package com.lts.jobtracker.support;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.commons.utils.DateUtils;
import com.lts.core.constant.Constants;
import com.lts.core.constant.Level;
import com.lts.core.domain.Job;
import com.lts.core.domain.JobWrapper;
import com.lts.core.exception.RemotingSendException;
import com.lts.core.exception.RequestTimeoutException;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobPullRequest;
import com.lts.core.protocol.command.JobPushRequest;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.core.commons.utils.Holder;
import com.lts.core.support.SystemClock;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.netty.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.domain.TaskTrackerNode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         任务分发管理
 */
public class JobPusher {

    private final Logger LOGGER = LoggerFactory.getLogger(JobPusher.class);
    private JobTrackerApplication application;
    private final ExecutorService executor;

    public JobPusher(JobTrackerApplication application) {
        this.application = application;
        executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 5, new NamedThreadFactory(JobPusher.class.getSimpleName()));
    }

    public void push(final RemotingServerDelegate remotingServer, final JobPullRequest request) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    pushJob(remotingServer, request);
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
    private void pushJob(RemotingServerDelegate remotingServer, JobPullRequest request) {

        String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        // 更新TaskTracker的可用线程数
        application.getTaskTrackerManager().updateTaskTrackerAvailableThreads(nodeGroup,
                identity, request.getAvailableThreads(), request.getTimestamp());
        TaskTrackerNode taskTrackerNode = application.getTaskTrackerManager().
                getTaskTrackerNode(nodeGroup, identity);

        if (taskTrackerNode == null) {
            return;
        }

        int availableThreads = taskTrackerNode.getAvailableThread().get();

        while (availableThreads > 0) {
            // 推送任务
            PushResult result = sendJob(remotingServer, taskTrackerNode);
            if (result == PushResult.SUCCESS) {
                availableThreads = taskTrackerNode.getAvailableThread().decrementAndGet();
            } else {
                break;
            }
        }
    }

    private enum PushResult {
        NO_JOB, // 没有任务可执行
        SUCCESS, //推送成功
        FAILED      //推送失败
    }

    /**
     * 是否推送成功
     *
     * @param remotingServer
     * @param taskTrackerNode
     * @return
     */
    private PushResult sendJob(RemotingServerDelegate remotingServer, TaskTrackerNode taskTrackerNode) {

        String nodeGroup = taskTrackerNode.getNodeGroup();
        String identity = taskTrackerNode.getIdentity();

        // 从mongo 中取一个可运行的job
        JobPo jobPo = application.getExecutableJobQueue().take(nodeGroup, identity);

        if (jobPo == null) {
            return PushResult.NO_JOB;
        }

        JobPushRequest body = application.getCommandBodyWrapper().wrapper(new JobPushRequest());
        body.setJobWrapper(JobDomainConverter.convert(jobPo));
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
                LOGGER.debug("Job push failed! nodeGroup=" + nodeGroup + ", identity=" + identity + ", job=" + jobPo);
            }
            application.getExecutableJobQueue().resume(jobPo);
            return PushResult.FAILED;
        }
        try {
            application.getExecutingJobQueue().add(jobPo);
        } catch (DuplicateJobException e) {
            // ignore
        }
        application.getExecutableJobQueue().remove(jobPo.getTaskTrackerNodeGroup(), jobPo.getJobId());
        // 记录日志

        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.SENT);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        application.getJobLogger().log(jobLogPo);

        return PushResult.SUCCESS;
    }
}
