package com.lts.job.tracker.support;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeManager;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.domain.LogType;
import com.lts.job.core.protocol.JobProtos;
import com.lts.job.core.protocol.command.JobAskRequest;
import com.lts.job.core.protocol.command.JobAskResponse;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.support.SingletonBeanContext;
import com.lts.job.core.repository.JobMongoRepository;
import com.lts.job.core.repository.po.JobPo;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.remoting.protocol.RemotingCommand;
import com.lts.job.remoting.protocol.RemotingProtos;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.channel.ChannelWrapper;
import com.lts.job.tracker.domain.TaskTrackerNode;
import com.lts.job.tracker.logger.JobLogger;
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

    private static JobMongoRepository jobRepository;
    // 5 分钟没有收到反馈信息 (并且该节点不存在了)，表示这个任务已经死掉了
    private static final long MAX_DEAD_CHECK_TIME = 5 * 60 * 1000;
    // 5 分钟没有收到反馈信息 并且该节点存在, 那么主动去询问taskTracker 这个任务是否在执行, 如果没有，则表示这个任务已经死掉了
    private static final long MAX_TIME_OUT = 5 * 60 * 1000;

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    public DeadJobChecker() {
        jobRepository = SingletonBeanContext.getBean(JobMongoRepository.class);
    }

    public void start() {
        ScheduledFuture<?> scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
                    // 一般来说这个是没有多大的，我就不分页去查询了
                    List<JobPo> jobPos = jobRepository.getDeadJob(MAX_DEAD_CHECK_TIME);
                    if (jobPos != null && jobPos.size() > 0) {
                        List<Node> nodes = NodeManager.getNodeList(NodeType.TASK_TRACKER);
                        HashSet<String/*identity*/> identities = new HashSet<String>();
                        if (CollectionUtils.isNotEmpty(nodes)) {
                            for (Node node : nodes) {
                                identities.add(node.getIdentity());
                            }
                        }

                        Map<TaskTrackerNode/*执行的TaskTracker节点 identity*/, List<String/*jobId*/>> timeoutMap = new HashMap<TaskTrackerNode, List<String>>();
                        for (JobPo jobPo : jobPos) {
                            if (!identities.contains(jobPo.getTaskTracker())) {
                                fixedDeadJob(jobPo);
                            } else {
                                // 如果节点存在，并且超时了, 那么去主动询问taskTracker 这个任务是否在执行中
                                if (System.currentTimeMillis() - jobPo.getGmtModify() > MAX_TIME_OUT) {
                                    TaskTrackerNode taskTrackerNode = new TaskTrackerNode(jobPo.getTaskTracker(), jobPo.getTaskTrackerNodeGroup());
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
                            RemotingServerDelegate remotingServer = RemotingServerManager.getRemotingServer();
                            for (Map.Entry<TaskTrackerNode, List<String>> entry : timeoutMap.entrySet()) {
                                TaskTrackerNode taskTrackerNode = entry.getKey();
                                ChannelWrapper channelWrapper = ChannelManager.getChannel(taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                                if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                                    JobAskRequest requestBody = new JobAskRequest();
                                    requestBody.setJobIds(entry.getValue());
                                    RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_ASK.code(), requestBody);
                                    RemotingCommand response = remotingServer.invokeSync(channelWrapper.getChannel(), request);
                                    if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                                        JobAskResponse responseBody = response.getBody();
                                        List<String> deadJobIds = responseBody.getJobIds();
                                        if (deadJobIds != null) {
                                            Thread.sleep(1000L);     // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                                            for (String deadJobId : deadJobIds) {
                                                JobPo jobPo = new JobPo();
                                                jobPo.setJobId(deadJobId);
                                                fixedDeadJob(jobPo);
                                            }
                                        }
                                    }
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
    public static void fixedDeadLock(Node node) {
        try {
            List<JobPo> jobPos = jobRepository.getJobByTaskTracker(node.getIdentity());
            if (CollectionUtils.isNotEmpty(jobPos)) {
                for (JobPo jobPo : jobPos) {
                    fixedDeadJob(jobPo);
                }
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    private static void fixedDeadJob(JobPo jobPo) {
        jobRepository.setJobRunnable(jobPo);
        JobLogger.log(jobPo, LogType.FIXED_DEAD);
        LOGGER.info("修复死掉的任务成功! {}", jobPo);
    }

    public void stop() {
        FIXED_EXECUTOR_SERVICE.shutdown();
    }

}
