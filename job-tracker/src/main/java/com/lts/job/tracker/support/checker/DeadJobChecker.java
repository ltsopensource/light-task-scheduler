package com.lts.job.tracker.support.checker;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.Level;
import com.lts.job.core.domain.LogType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.protocol.command.JobAskRequest;
import com.lts.job.core.protocol.command.JobAskResponse;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.remoting.InvokeCallback;
import com.lts.job.remoting.netty.ResponseFuture;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.channel.ChannelWrapper;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.domain.TaskTrackerNode;
import com.lts.job.tracker.logger.JobLogger;
import com.lts.job.tracker.logger.domain.JobLogPo;
import com.lts.job.tracker.queue.JobPo;
import com.lts.job.tracker.queue.JobQueue;
import com.lts.job.tracker.support.JobDomainConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         死掉的任务
 *         1. 分发出去的，并且执行节点不存在的任务
 *         2. 分发出去，执行节点还在, 但是没有在执行的任务
 */
public class DeadJobChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadJobChecker.class);

    // 5 分钟没有收到反馈信息 (并且该节点不存在了)，表示这个任务已经死掉了
    private static final long MAX_DEAD_CHECK_TIME = 5 * 60 * 1000;
    // 5 分钟没有收到反馈信息 并且该节点存在, 那么主动去询问taskTracker 这个任务是否在执行, 如果没有，则表示这个任务已经死掉了
    private static final long MAX_TIME_OUT = 5 * 60 * 1000;

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private JobTrackerApplication application;
    private ChannelManager channelManager;
    private CommandBodyWrapper commandBodyWrapper;
    private JobLogger jobLogger;
    private JobQueue jobQueue;

    public DeadJobChecker(JobTrackerApplication application) {
        this.application = application;
        this.channelManager = application.getChannelManager();
        this.commandBodyWrapper = application.getCommandBodyWrapper();
        this.jobLogger = application.getJobLogger();
        this.jobQueue = application.getJobQueue();
    }

    private volatile boolean start;
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        if (start) {
            return;
        }
        start = true;

        scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
                    // 一般来说这个是没有多大的，我就不分页去查询了
                    List<JobPo> jobPos = jobQueue.getByLimitExecTime(MAX_DEAD_CHECK_TIME);
                    if (jobPos != null && jobPos.size() > 0) {
                        List<Node> nodes = application.getNodeManager().getNodeList(NodeType.TASK_TRACKER);
                        HashSet<String/*identity*/> identities = new HashSet<String>();
                        if (CollectionUtils.isNotEmpty(nodes)) {
                            for (Node node : nodes) {
                                identities.add(node.getIdentity());
                            }
                        }

                        Map<TaskTrackerNode/*执行的TaskTracker节点 identity*/, List<String/*jobId*/>> timeoutMap = new HashMap<TaskTrackerNode, List<String>>();
                        for (JobPo jobPo : jobPos) {
                            if (!identities.contains(jobPo.getTaskTrackerIdentity())) {
                                fixedDeadJob(jobPo);
                            } else {
                                // 如果节点存在，并且超时了, 那么去主动询问taskTracker 这个任务是否在执行中
                                if (System.currentTimeMillis() - jobPo.getGmtModify() > MAX_TIME_OUT) {
                                    TaskTrackerNode taskTrackerNode = new TaskTrackerNode(jobPo.getTaskTrackerIdentity(), jobPo.getTaskTrackerNodeGroup());
                                    List<String> jobIds = timeoutMap.get(taskTrackerNode);
                                    if (jobIds == null) {
                                        jobIds = new ArrayList<String>();
                                        timeoutMap.put(taskTrackerNode, jobIds);
                                    }
                                    jobIds.add(jobPo.getJobId());
                                }
                            }
                        }

                        if (CollectionUtils.isNotEmpty(timeoutMap)) {
                            RemotingServerDelegate remotingServer = application.getRemotingServer();
                            for (Map.Entry<TaskTrackerNode, List<String>> entry : timeoutMap.entrySet()) {
                                TaskTrackerNode taskTrackerNode = entry.getKey();
                                ChannelWrapper channelWrapper = channelManager.getChannel(taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                                if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                                    JobAskRequest requestBody = commandBodyWrapper.wrapper(new JobAskRequest());
                                    requestBody.setJobIds(entry.getValue());
                                    RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_ASK.code(), requestBody);
                                    remotingServer.invokeAsync(channelWrapper.getChannel(), request, new InvokeCallback() {
                                        @Override
                                        public void operationComplete(ResponseFuture responseFuture) {
                                            RemotingCommand response = responseFuture.getResponseCommand();
                                            if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                                                JobAskResponse responseBody = response.getBody();
                                                List<String> deadJobIds = responseBody.getJobIds();
                                                if (deadJobIds != null) {
                                                    try {
                                                        Thread.sleep(1000L);     // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    for (String deadJobId : deadJobIds) {
                                                        JobPo jobPo = new JobPo();
                                                        jobPo.setJobId(deadJobId);
                                                        fixedDeadJob(jobPo);
                                                    }
                                                }
                                            }
                                        }
                                    });

                                }
                            }
                        }

                    }
                } catch (Throwable t) {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }, 2 * 60, 3 * 60, TimeUnit.SECONDS);// 3分钟执行一次
    }

    /**
     * 根据停止的节点修复死锁
     *
     * @param node
     */
    public void fixedDeadLock(Node node) {
        try {
            // 1. 判断这个节点的channel是否存在
            ChannelWrapper channelWrapper = application.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
            if(channelWrapper == null || channelWrapper.getChannel() == null
            || channelWrapper.isClosed()){
                List<JobPo> jobPos = jobQueue.getRunningJob(node.getIdentity());
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
        jobQueue.resume(jobPo);
        try {
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setLevel(Level.WARN);
            jobLogPo.setLogType(LogType.FIXED_DEAD);
            jobLogger.log(jobLogPo);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        LOGGER.info("修复死掉的任务成功! {}", JSONUtils.toJSONString(jobPo));
    }

    public void stop() {
        if (start) {
            start = false;
            scheduledFuture.cancel(true);
            FIXED_EXECUTOR_SERVICE.shutdown();
        }
    }

}
