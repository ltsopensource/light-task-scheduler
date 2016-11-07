package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.DotLogUtils;
import com.github.ltsopensource.core.commons.utils.Holder;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.exception.RemotingSendException;
import com.github.ltsopensource.core.exception.RequestTimeoutException;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobPullRequest;
import com.github.ltsopensource.core.protocol.command.JobPushRequest;
import com.github.ltsopensource.core.protocol.command.JobPushResponse;
import com.github.ltsopensource.core.remoting.RemotingServerDelegate;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.domain.TaskTrackerNode;
import com.github.ltsopensource.jobtracker.monitor.JobTrackerMStatReporter;
import com.github.ltsopensource.jobtracker.sender.JobPushResult;
import com.github.ltsopensource.jobtracker.sender.JobSender;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.remoting.AsyncCallback;
import com.github.ltsopensource.remoting.ResponseFuture;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         任务分发管理
 */
public class JobPusher {

    private final Logger LOGGER = LoggerFactory.getLogger(JobPusher.class);
    private JobTrackerAppContext appContext;
    private final ExecutorService executorService;
    private final ExecutorService pushExecutorService;
    private JobTrackerMStatReporter stat;
    private RemotingServerDelegate remotingServer;
    private int jobPushBatchSize = 10;
    private ConcurrentHashMap<String, AtomicBoolean> PUSHING_FLAG = new ConcurrentHashMap<String, AtomicBoolean>();

    public JobPusher(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.executorService = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 5,
                new NamedThreadFactory(JobPusher.class.getSimpleName() + "-Executor", true));
        int processorSize = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_PUSHER_THREAD_NUM, Constants.DEFAULT_JOB_TRACKER_PUSHER_THREAD_NUM);
        this.pushExecutorService = Executors.newFixedThreadPool(processorSize,
                new NamedThreadFactory(JobPusher.class.getSimpleName() + "-AsyncPusher", true));
        this.stat = (JobTrackerMStatReporter) appContext.getMStatReporter();
        this.remotingServer = appContext.getRemotingServer();

        this.jobPushBatchSize = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_PUSH_BATCH_SIZE, Constants.DEFAULT_JOB_TRACKER_PUSH_BATCH_SIZE);
    }

    public void push(final JobPullRequest request) {

        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    push0(request);
                } catch (Exception e) {
                    LOGGER.error("Job push failed!", e);
                }
            }
        });
    }

    /**
     * 是否正在推送
     */
    private AtomicBoolean getPushingFlag(TaskTrackerNode taskTrackerNode) {
        AtomicBoolean flag = PUSHING_FLAG.get(taskTrackerNode.getIdentity());
        if (flag == null) {
            flag = new AtomicBoolean(false);
            AtomicBoolean exist = PUSHING_FLAG.putIfAbsent(taskTrackerNode.getIdentity(), flag);
            if (exist != null) {
                flag = exist;
            }
        }
        return flag;
    }

    private void push0(final JobPullRequest request) {

        String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        // 更新TaskTracker的可用线程数
        appContext.getTaskTrackerManager().updateTaskTrackerAvailableThreads(nodeGroup,
                identity, request.getAvailableThreads(), request.getTimestamp());

        final TaskTrackerNode taskTrackerNode = appContext.getTaskTrackerManager().
                getTaskTrackerNode(nodeGroup, identity);

        if (taskTrackerNode == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , didn't have node.", nodeGroup, identity);
            }
            return;
        }

        int availableThread = taskTrackerNode.getAvailableThread().get();
        if (availableThread <= 0) {
            return;
        }

        AtomicBoolean pushingFlag = getPushingFlag(taskTrackerNode);
        if (pushingFlag.compareAndSet(false, true)) {
            try {
                final int batchSize = jobPushBatchSize;

                int it = availableThread % batchSize == 0 ? availableThread / batchSize : availableThread / batchSize + 1;

                final CountDownLatch latch = new CountDownLatch(it);

                for (int i = 1; i <= it; i++) {
                    int size = batchSize;
                    if (i == it) {
                        size = availableThread - batchSize * (it - 1);
                    }
                    final int finalSize = size;
                    pushExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 推送任务
                                send(remotingServer, finalSize, taskTrackerNode);
                            } catch (Throwable t) {
                                LOGGER.error("Error on Push Job to {}", taskTrackerNode, t);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                DotLogUtils.dot("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , pushing finished. batchTimes:{}, size:{}", nodeGroup, identity, it, availableThread);
            } finally {
                pushingFlag.compareAndSet(true, false);
            }
        }
    }

    /**
     * 是否推送成功
     */
    private JobPushResult send(final RemotingServerDelegate remotingServer, int size, final TaskTrackerNode taskTrackerNode) {

        final String nodeGroup = taskTrackerNode.getNodeGroup();
        final String identity = taskTrackerNode.getIdentity();

        JobSender.SendResult sendResult = appContext.getJobSender().send(nodeGroup, identity, size, new JobSender.SendInvoker() {
            @Override
            public JobSender.SendResult invoke(final List<JobPo> jobPos) {

                // 发送给TaskTracker执行
                JobPushRequest body = appContext.getCommandBodyWrapper().wrapper(new JobPushRequest());
                body.setJobMetaList(JobDomainConverter.convert(jobPos));
                RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.PUSH_JOB.code(), body);

                // 是否分发推送任务成功
                final Holder<Boolean> pushSuccess = new Holder<Boolean>(false);

                final CountDownLatch latch = new CountDownLatch(1);
                try {
                    remotingServer.invokeAsync(taskTrackerNode.getChannel().getChannel(), commandRequest, new AsyncCallback() {
                        @Override
                        public void operationComplete(ResponseFuture responseFuture) {
                            try {
                                RemotingCommand responseCommand = responseFuture.getResponseCommand();
                                if (responseCommand == null) {
                                    LOGGER.warn("Job push failed! response command is null!");
                                    return;
                                }
                                if (responseCommand.getCode() == JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code()) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Job push success! nodeGroup=" + nodeGroup + ", identity=" + identity + ", jobList=" + JSON.toJSONString(jobPos));
                                    }
                                    pushSuccess.set(true);
                                    stat.incPushJobNum(jobPos.size());
                                } else if (responseCommand.getCode() == JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code()) {
                                    JobPushResponse jobPushResponse = responseCommand.getBody();
                                    if (jobPushResponse != null && CollectionUtils.isNotEmpty(jobPushResponse.getFailedJobIds())) {
                                        // 修复任务
                                        for (String jobId : jobPushResponse.getFailedJobIds()) {
                                            for (JobPo jobPo : jobPos) {
                                                if (jobId.equals(jobPo.getJobId())) {
                                                    resumeJob(jobPo);
                                                    break;
                                                }
                                            }
                                        }
                                        stat.incPushJobNum(jobPos.size() - jobPushResponse.getFailedJobIds().size());
                                    } else {
                                        stat.incPushJobNum(jobPos.size());
                                    }
                                    pushSuccess.set(true);
                                }

                            } finally {
                                latch.countDown();
                            }
                        }
                    });

                } catch (RemotingSendException e) {
                    LOGGER.error("Remoting send error, jobPos={}", JSON.toJSONObject(jobPos), e);
                    return new JobSender.SendResult(false, JobPushResult.SENT_ERROR);
                }

                try {
                    latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RequestTimeoutException(e);
                }

                if (!pushSuccess.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job push failed! nodeGroup=" + nodeGroup + ", identity=" + identity + ", jobs=" + JSON.toJSONObject(jobPos));
                    }
                    for (JobPo jobPo : jobPos) {
                        resumeJob(jobPo);
                    }
                    return new JobSender.SendResult(false, JobPushResult.SENT_ERROR);
                }

                return new JobSender.SendResult(true, JobPushResult.SUCCESS);
            }
        });

        return (JobPushResult) sendResult.getReturnValue();
    }

    private void resumeJob(JobPo jobPo) {

        // 队列切回来
        boolean needResume = true;
        try {
            jobPo.setIsRunning(true);
            appContext.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
            needResume = false;
        }
        appContext.getExecutingJobQueue().remove(jobPo.getJobId());
        if (needResume) {
            appContext.getExecutableJobQueue().resume(jobPo);
        }
    }
}
