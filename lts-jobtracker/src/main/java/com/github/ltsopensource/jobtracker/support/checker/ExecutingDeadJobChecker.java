package com.github.ltsopensource.jobtracker.support.checker;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.QuietUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.exception.RemotingSendException;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobAskRequest;
import com.github.ltsopensource.core.protocol.command.JobAskResponse;
import com.github.ltsopensource.core.remoting.RemotingServerDelegate;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.channel.ChannelWrapper;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.jobtracker.monitor.JobTrackerMStatReporter;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.remoting.AsyncCallback;
import com.github.ltsopensource.remoting.Channel;
import com.github.ltsopensource.remoting.ResponseFuture;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.remoting.protocol.RemotingProtos;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

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

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-ExecutingJobQueue-Fix-Executor", true));

    private JobTrackerAppContext appContext;
    private JobTrackerMStatReporter stat;

    public ExecutingDeadJobChecker(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.stat = (JobTrackerMStatReporter) appContext.getMStatReporter();
    }

    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                int fixCheckPeriodSeconds = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_EXECUTING_JOB_FIX_CHECK_INTERVAL_SECONDS, 30);
                if (fixCheckPeriodSeconds < 5) {
                    fixCheckPeriodSeconds = 5;
                } else if (fixCheckPeriodSeconds > 5 * 60) {
                    fixCheckPeriodSeconds = 5 * 60;
                }

                scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!appContext.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            checkAndFix();
                        } catch (Throwable t) {
                            LOGGER.error("Check executing dead job error ", t);
                        }
                    }
                }, fixCheckPeriodSeconds, fixCheckPeriodSeconds, TimeUnit.SECONDS);
            }
            LOGGER.info("Executing dead job checker started!");
        } catch (Throwable e) {
            LOGGER.error("Executing dead job checker start failed!", e);
        }
    }

    private void checkAndFix() throws RemotingSendException {

        // 30s没有收到反馈信息，需要去检查这个任务是否还在执行
        int maxDeadCheckTime = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_EXECUTING_JOB_FIX_DEADLINE_SECONDS, 20);
        if (maxDeadCheckTime < 10) {
            maxDeadCheckTime = 10;
        } else if (maxDeadCheckTime > 5 * 60) {
            maxDeadCheckTime = 5 * 60;
        }

        // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
        // 一般来说这个是没有多大的，我就不分页去查询了
        List<JobPo> maybeDeadJobPos = appContext.getExecutingJobQueue().getDeadJobs(
                SystemClock.now() - maxDeadCheckTime * 1000);
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
                ChannelWrapper channelWrapper = appContext.getChannelManager().getChannel(taskTrackerNodeGroup, NodeType.TASK_TRACKER, taskTrackerIdentity);
                if (channelWrapper == null && taskTrackerIdentity != null) {
                    Long offlineTimestamp = appContext.getChannelManager().getOfflineTimestamp(taskTrackerIdentity);
                    // 已经离线太久，直接修复
                    if (offlineTimestamp == null || SystemClock.now() - offlineTimestamp > Constants.DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
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
            RemotingServerDelegate remotingServer = appContext.getRemotingServer();
            List<String> jobIds = new ArrayList<String>(jobPos.size());
            for (JobPo jobPo : jobPos) {
                jobIds.add(jobPo.getJobId());
            }
            JobAskRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobAskRequest());
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

                            // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                            QuietUtils.sleep(appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_FIX_EXECUTING_JOB_WAITING_MILLS, 1000L));

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

            // 已经被移除了
            if (appContext.getExecutingJobQueue().getJob(jobPo.getJobId()) == null) {
                return;
            }

            jobPo.setGmtModified(SystemClock.now());
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            // 1. add to executable queue
            try {
                appContext.getExecutableJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
            }

            // 2. remove from executing queue
            appContext.getExecutingJobQueue().remove(jobPo.getJobId());

            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setSuccess(true);
            jobLogPo.setLevel(Level.WARN);
            jobLogPo.setLogType(LogType.FIXED_DEAD);
            appContext.getJobLogger().log(jobLogPo);

            stat.incFixExecutingJobNum();

        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        LOGGER.info("checkAndFix dead job ! {}", JSON.toJSONString(jobPo));
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
