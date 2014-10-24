package com.lts.job.tracker.channel;

import com.lts.job.common.cluster.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 管理channel
 */
public class ChannelManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);
    // 客户端列表 (要保证同一个group的node要是无状态的)
    private static final ConcurrentHashMap<String/*clientGroup*/, List<ChannelWrapper>> clientChannelMap = new ConcurrentHashMap<String, List<ChannelWrapper>>();
    // 任务节点列表
    private static final ConcurrentHashMap<String/*taskTrackerGroup*/, List<ChannelWrapper>> taskTrackerChannelMap = new ConcurrentHashMap<String, List<ChannelWrapper>>();
    // 用来定时检查已经关闭的channel
    private static final ScheduledExecutorService channelCheckExecutorService = Executors.newScheduledThreadPool(1);

    static {

        channelCheckExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                checkCloseChannel(clientChannelMap);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("JobClient Channel Pool " + clientChannelMap);
                }
                checkCloseChannel(taskTrackerChannelMap);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("TaskTracker Channel Pool " + taskTrackerChannelMap);
                }

            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    private ChannelManager() {
    }

    /**
     * 检查 关闭的channel
     *
     * @param channelMap
     */
    private static void checkCloseChannel(ConcurrentHashMap<String, List<ChannelWrapper>> channelMap) {
        for (Map.Entry<String, List<ChannelWrapper>> entry : channelMap.entrySet()) {
            List<ChannelWrapper> channels = entry.getValue();
            List<ChannelWrapper> removeList = new ArrayList<ChannelWrapper>();
            for (ChannelWrapper channel : channels) {
                if (channel.isClosed()) {
                    removeList.add(channel);
                    LOGGER.info("close channel=" + channel);
                }
            }
            channels.removeAll(removeList);
        }
    }

    public static List<ChannelWrapper> getChannels(String nodeGroup, NodeType nodeType) {
        if (nodeType == NodeType.CLIENT) {
            return clientChannelMap.get(nodeGroup);
        } else if (nodeType == NodeType.TASK_TRACKER) {
            return taskTrackerChannelMap.get(nodeGroup);
        }
        return null;
    }

    /**
     * 根据 节点唯一编号得到 channel
     * @param nodeGroup
     * @param nodeType
     * @param identity
     * @return
     */
    public static ChannelWrapper getChannel(String nodeGroup, NodeType nodeType, String identity) {
        List<ChannelWrapper> channelWrappers = getChannels(nodeGroup, nodeType);
        if (channelWrappers != null && channelWrappers.size() != 0) {
            for (ChannelWrapper channelWrapper : channelWrappers) {
                if (channelWrapper.getIdentity().equals(identity)) {
                    return channelWrapper;
                }
            }
        }
        return null;
    }

    /**
     * 添加channel
     *
     * @param channel
     */
    public static void offerChannel(ChannelWrapper channel) {
        String nodeGroup = channel.getNodeGroup();
        NodeType nodeType = channel.getNodeType();
        List<ChannelWrapper> channels = getChannels(nodeGroup, nodeType);
        if (channels == null) {
            channels = new ArrayList<ChannelWrapper>();
            if (nodeType == NodeType.CLIENT) {
                clientChannelMap.put(nodeGroup, channels);
            } else if (nodeType == NodeType.TASK_TRACKER) {
                taskTrackerChannelMap.put(nodeGroup, channels);
            }
            channels.add(channel);
            LOGGER.info("new connected channel=" + channel);
        } else {
            if (!channels.contains(channel)) {
                channels.add(channel);
                LOGGER.info("new connected channel=" + channel);
            }
        }
    }

    public static void removeChannel(ChannelWrapper channel) {
        String nodeGroup = channel.getNodeGroup();
        NodeType nodeType = channel.getNodeType();
        List<ChannelWrapper> channels = getChannels(nodeGroup, nodeType);
        if (channels != null) {
            channels.remove(channel);
            LOGGER.info("remove channel=" + channel);
        }
    }
}
