package com.lts.job.tracker.support;


import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.channel.ChannelWrapper;
import com.lts.job.tracker.domain.TaskTrackerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 *         Task Tracker 管理器 (对 TaskTracker 节点的记录 和 可用线程的记录)
 */
public class TaskTrackerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrackerManager.class);
    // 单例
    private final ConcurrentHashMap<String/*nodeGroup*/, ConcurrentHashSet<TaskTrackerNode>> NODE_MAP = new ConcurrentHashMap<String, ConcurrentHashSet<TaskTrackerNode>>();
    private ChannelManager channelManager;

    public TaskTrackerManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    /**
     * 添加节点
     *
     * @param node
     */
    public void addNode(Node node) {
        //  channel 可能为 null
        ChannelWrapper channel = channelManager.getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        ConcurrentHashSet<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());

        synchronized (NODE_MAP) {
            if (taskTrackerNodes == null) {
                taskTrackerNodes = new ConcurrentHashSet<TaskTrackerNode>();
                NODE_MAP.put(node.getGroup(), taskTrackerNodes);
            }
        }

        TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getGroup(), node.getThreads(), node.getIdentity(), channel);
        LOGGER.info("添加TaskTracker节点:{}", taskTrackerNode);
        taskTrackerNodes.add(taskTrackerNode);
    }

    /**
     * 删除节点
     *
     * @param node
     */
    public void removeNode(Node node) {
        ConcurrentHashSet<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());
        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            TaskTrackerNode taskTrackerNode = new TaskTrackerNode(node.getIdentity());
            taskTrackerNode.setNodeGroup(node.getGroup());
            LOGGER.info("删除TaskTracker节点:{}", taskTrackerNode);
            taskTrackerNodes.remove(taskTrackerNode);
        }
    }

    public TaskTrackerNode getTaskTrackerNode(String nodeGroup, String identity) {
        ConcurrentHashSet<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);
        if (taskTrackerNodes == null || taskTrackerNodes.size() == 0) {
            return null;
        }

        for (TaskTrackerNode taskTrackerNode : taskTrackerNodes) {
            if (taskTrackerNode.getIdentity().equals(identity)) {
                if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
                    // 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
                    ChannelWrapper channel = channelManager.getChannel(taskTrackerNode.getNodeGroup(), NodeType.TASK_TRACKER, taskTrackerNode.getIdentity());
                    if (channel != null) {
                        // 更新channel
                        taskTrackerNode.setChannel(channel);
                        LOGGER.info("更新节点channel , taskTackerNode={}", taskTrackerNode);
                        return taskTrackerNode;
                    }
                }else{
                    // 只有当channel正常的时候才返回
                    return taskTrackerNode;
                }
            }
        }
        return null;
    }

    /**
     * 更新节点的 可用线程数
     *
     * @param nodeGroup
     * @param identity
     * @param availableThreads
     * @param timestamp        时间戳, 只有当 时间戳大于上次更新的时间 才更新可用线程数
     */
    public void updateTaskTrackerAvailableThreads(String nodeGroup, String identity, Integer availableThreads, Long timestamp) {

        ConcurrentHashSet<TaskTrackerNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);

        if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
            for (TaskTrackerNode trackerNode : taskTrackerNodes) {
                if (trackerNode.getIdentity().equals(identity) && trackerNode.getTimestamp() <= timestamp) {
                    trackerNode.setAvailableThread(availableThreads);
                    trackerNode.setTimestamp(timestamp);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("更新节点线程数: {}", trackerNode);
                    }
                }
            }
        }
    }
}
