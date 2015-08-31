package com.lts.jobtracker.support.checker;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.constant.Level;
import com.lts.core.exception.RemotingSendException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobAskRequest;
import com.lts.core.protocol.command.JobAskResponse;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.channel.ChannelWrapper;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.domain.TaskTrackerNode;
import com.lts.jobtracker.monitor.JobTrackerMonitor;
import com.lts.jobtracker.support.JobDomainConverter;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.lts.remoting.InvokeCallback;
import com.lts.remoting.netty.ResponseFuture;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.remoting.protocol.RemotingProtos;

import java.util.*;
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

    // 2 分钟没有收到反馈信息 (并且该节点不存在了)，表示这个任务已经死掉了
    private static final long MAX_DEAD_CHECK_TIME = 2 * 60 * 1000;
    // 1 分钟没有收到反馈信息 并且该节点存在, 那么主动去询问taskTracker 这个任务是否在执行, 如果没有，则表示这个任务已经死掉了
    private static final long MAX_TIME_OUT = 60 * 1000;

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
                            fix();
                        } catch (Throwable t) {
                            LOGGER.error(t.getMessage(), t);
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
        List<JobPo> jobPos = application.getExecutingJobQueue().getDeadJobs(
                SystemClock.now() - MAX_DEAD_CHECK_TIME);
        if (jobPos != null && jobPos.size() > 0) {
            List<Node> nodes = application.getSubscribedNodeManager().getNodeList(NodeType.TASK_TRACKER);
            HashSet<String/*identity*/> identities = new HashSet<String>();
            if (CollectionUtils.isNotEmpty(nodes)) {
                for (Node node : nodes) {
                    identities.add(node.getIdentity());
                }
            }

            Map<TaskTrackerNode/*执行的TaskTracker节点 identity*/, List<JobPo/*jobId*/>> timeoutMap = new HashMap<TaskTrackerNode, List<JobPo>>();
            for (JobPo jobPo : jobPos) {
                if (!identities.contains(jobPo.getTaskTrackerIdentity())) {
                    fixedDeadJob(jobPo);
                } else {
                    // 如果节点存在，并且超时了, 那么去主动询问taskTracker 这个任务是否在执行中
                    if (SystemClock.now() - jobPo.getGmtModified() > MAX_TIME_OUT) {
                        TaskTrackerNode taskTrackerNode = new TaskTrackerNode(jobPo.getTaskTrackerIdentity(), jobPo.getTaskTrackerNodeGroup());
                        List<JobPo> jobPosList = timeoutMap.get(taskTrackerNode);
                        if (jobPosList == null) {
                            jobPosList = new ArrayList<JobPo>();
                            timeoutMap.put(taskTrackerNode, jobPosList);
                        }
                        jobPosList.add(jobPo);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(timeoutMap)) {
                RemotingServerDelegate remotingServer = application.getRemotingServer();
                for (Map.Entry<TaskTrackerNode, List<JobPo>> entry : timeoutMap.entrySet()) {
                    TaskTrackerNode taskTrackerNode = entry.getKey();
                    ChannelWrapper channelWrapper = application.getChannelManager().getChannel(taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                    if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                        JobAskRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobAskRequest());

                        final List<JobPo> jobPoList = entry.getValue();
                        List<String> jobIds = new ArrayList<String>(jobPoList.size());
                        for (JobPo jobPo : jobPoList) {
                            jobIds.add(jobPo.getJobId());
                        }
                        requestBody.setJobIds(jobIds);
                        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_ASK.code(), requestBody);
                        remotingServer.invokeAsync(channelWrapper.getChannel(), request, new InvokeCallback() {
                            @Override
                            public void operationComplete(ResponseFuture responseFuture) {
                                RemotingCommand response = responseFuture.getResponseCommand();
                                if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                                    JobAskResponse responseBody = response.getBody();
                                    List<String> deadJobIds = responseBody.getJobIds();
                                    if (CollectionUtils.isNotEmpty(deadJobIds)) {
                                        try {
                                            Thread.sleep(1000L);     // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        for (JobPo jobPo : jobPoList) {
                                            if (deadJobIds.contains(jobPo.getJobId())) {
                                                fixedDeadJob(jobPo);
                                            }
                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            }
        }
    }

    /**
     * 根据停止的节点修复死锁
     *
     * @param node
     */
    public void fixedDeadNodeJob(Node node) {
        try {
            // 1. 判断这个节点的channel是否存在
            ChannelWrapper channelWrapper = application.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
            if (channelWrapper == null || channelWrapper.getChannel() == null
                    || channelWrapper.isClosed()) {
                List<JobPo> jobPos = application.getExecutingJobQueue().getJobs(node.getIdentity());
                if (CollectionUtils.isNotEmpty(jobPos)) {
                    for (JobPo jobPo : jobPos) {
                        fixedDeadJob(jobPo);
                    }
                }
            }
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    private void fixedDeadJob(JobPo jobPo) {
        try {
            jobPo.setIsRunning(false);
            // 1. add to executable queue
            try {
                application.getExecutableJobQueue().add(jobPo);
            } catch (DuplicateJobException ignore) {
                // ignore
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
        LOGGER.info("fix dead job ! {}", JSONUtils.toJSONString(jobPo));
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
