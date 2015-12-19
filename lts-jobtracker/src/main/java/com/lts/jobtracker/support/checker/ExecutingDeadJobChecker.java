package com.lts.jobtracker.support.checker;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.constant.Constants;
import com.lts.core.constant.Level;
import com.lts.core.exception.RemotingSendException;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobAskRequest;
import com.lts.core.protocol.command.JobAskResponse;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.channel.ChannelWrapper;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.monitor.JobTrackerMonitor;
import com.lts.jobtracker.support.JobDomainConverter;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.lts.remoting.AsyncCallback;
import com.lts.remoting.Channel;
import com.lts.remoting.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         死掉的任务
 *         1. 分发出去的，并且执行节点不存在的任务
 *         2. 分发出去，执行节点还在, 但是没有在执行的任务
 */
public class ExecutingDeadJobChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutingDeadJobChecker.class);

    // 2 分钟没有收到反馈信息，需要去检查这个任务是否还在执行
    private static final long MAX_DEAD_CHECK_TIME = 2 * 60 * 1000;

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private JobTrackerApplication application;
    private JobTrackerMonitor monitor;

    public ExecutingDeadJobChecker(JobTrackerApplication application) {
        this.application = application;
        this.monitor = (JobTrackerMonitor) application.getMonitor();
    }

    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!application.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            fix();
                        } catch (Throwable t) {
                            LOGGER.error("Check executing dead job error ", t);
                        }
                    }
                }, 30, 60, TimeUnit.SECONDS);// 1分钟执行一次
            }
            LOGGER.info("Executing dead job checker started!");
        } catch (Throwable e) {
            LOGGER.error("Executing dead job checker start failed!", e);
        }
    }

    private void fix() throws RemotingSendException {
        // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
        // 一般来说这个是没有多大的，我就不分页去查询了
        List<JobPo> maybeDeadJobPos = application.getExecutingJobQueue().getDeadJobs(
                SystemClock.now() - MAX_DEAD_CHECK_TIME);
        if (CollectionUtils.isNotEmpty(maybeDeadJobPos)) {

            Map<String/*taskTrackerIdentity*/, List<JobPo>> jobMap = new HashMap<String, List<JobPo>>();
            for (JobPo jobPo : maybeDeadJobPos) {
                List<JobPo> jobPos = jobMap.get(jobPo.getTaskTrackerIdentity());
                if (jobPos == null) {
                    jobPos = new ArrayList<JobPo>();
                    jobMap.put(jobPo.getTaskTrackerIdentity(), jobPos);
                }
                jobPos.add(jobPo);
            }

            for (Map.Entry<String, List<JobPo>> entry : jobMap.entrySet()) {
                String taskTrackerNodeGroup = entry.getValue().get(0).getTaskTrackerNodeGroup();
                String taskTrackerIdentity = entry.getKey();
                // 去查看这个TaskTrackerIdentity是否存活
                ChannelWrapper channelWrapper = application.getChannelManager().getChannel(taskTrackerNodeGroup, NodeType.TASK_TRACKER, taskTrackerIdentity);
                if (channelWrapper == null && taskTrackerIdentity != null) {
                    Long offlineTimestamp = application.getChannelManager().getOfflineTimestamp(taskTrackerIdentity);
                    // 已经离线太久，直接修复
                    if (offlineTimestamp == null || SystemClock.now() - offlineTimestamp > Constants.TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
                        // fixDeadJob
                        fixDeadJob(entry.getValue());
                    }
                } else {
                    // 去询问是否在执行该任务
                    if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                        askTimeoutJob(channelWrapper.getChannel(), entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * 向taskTracker询问执行中的任务
     */
    private void askTimeoutJob(Channel channel, final List<JobPo> jobPos) {
        try {
            RemotingServerDelegate remotingServer = application.getRemotingServer();
            List<String> jobIds = new ArrayList<String>(jobPos.size());
            for (JobPo jobPo : jobPos) {
                jobIds.add(jobPo.getJobId());
            }
            JobAskRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobAskRequest());
            requestBody.setJobIds(jobIds);
            RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_ASK.code(), requestBody);
            remotingServer.invokeAsync(channel, request, new AsyncCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    RemotingCommand response = responseFuture.getResponseCommand();
                    if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                        JobAskResponse responseBody = response.getBody();
                        List<String> deadJobIds = responseBody.getJobIds();
                        if (CollectionUtils.isNotEmpty(deadJobIds)) {
                            try {
                                // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                                Thread.sleep(1000L);
                            } catch (InterruptedException ignored) {
                            }
                            for (JobPo jobPo : jobPos) {
                                if (deadJobIds.contains(jobPo.getJobId())) {
                                    fixDeadJob(jobPo);
                                }
                            }
                        }
                    }
                }
            });
        } catch (RemotingSendException e) {
            LOGGER.error("Ask timeout Job error, ", e);
        }

    }

    private void fixDeadJob(List<JobPo> jobPos) {
        for (JobPo jobPo : jobPos) {
            fixDeadJob(jobPo);
        }
    }

    private void fixDeadJob(JobPo jobPo) {
        try {

            jobPo.setGmtModified(SystemClock.now());
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            // 1. add to executable queue
            try {
                application.getExecutableJobQueue().add(jobPo);
            } catch (DuplicateJobException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
            }

            // 2. remove from executing queue
            application.getExecutingJobQueue().remove(jobPo.getJobId());

            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLevel(Level.WARN);
            jobLogPo.setLogType(LogType.FIXED_DEAD);
            application.getJobLogger().log(jobLogPo);

            monitor.incFixExecutingJobNum();

        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        LOGGER.info("fix dead job ! {}", JSON.toJSONString(jobPo));
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executing dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executing dead job checker stop failed!", t);
        }
    }

}
