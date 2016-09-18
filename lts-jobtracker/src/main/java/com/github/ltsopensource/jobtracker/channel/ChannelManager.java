package com.github.ltsopensource.jobtracker.channel;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         管理channel
 */
public class ChannelManager {

    private final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);
    // 客户端列表 (要保证同一个group的node要是无状态的)
    private final ConcurrentHashMap<String/*clientGroup*/, List<ChannelWrapper>> clientChannelMap = new ConcurrentHashMap<String, List<ChannelWrapper>>();
    // 任务节点列表
    private final ConcurrentHashMap<String/*taskTrackerNodeGroup*/, List<ChannelWrapper>> taskTrackerChannelMap = new ConcurrentHashMap<String, List<ChannelWrapper>>();
    // 用来定时检查已经关闭的channel
    private final ScheduledExecutorService channelCheckExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-Channel-Checker", true));
    private ScheduledFuture<?> scheduledFuture;
    // 存储离线一定时间内的节点信息
    private final ConcurrentHashMap<String/*identity*/, Long> offlineTaskTrackerMap = new ConcurrentHashMap<String, Long>();
    // 用来清理离线时间很长的信息
    private final ScheduledExecutorService offlineTaskTrackerCheckExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-offline-TaskTracker-Checker", true));
    private ScheduledFuture<?> offlineTaskTrackerScheduledFuture;

    private AtomicBoolean start = new AtomicBoolean(false);

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = channelCheckExecutorService.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkCloseChannel(NodeType.JOB_CLIENT, clientChannelMap);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("JobClient Channel Pool " + clientChannelMap);
                            }
                            checkCloseChannel(NodeType.TASK_TRACKER, taskTrackerChannelMap);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("TaskTracker Channel Pool " + taskTrackerChannelMap);
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Check channel error!", t);
                        }
                    }
                }, 10, 10, TimeUnit.SECONDS);

                offlineTaskTrackerScheduledFuture = offlineTaskTrackerCheckExecutorService.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (offlineTaskTrackerMap.size() > 0) {
                                for (Map.Entry<String, Long> entry : offlineTaskTrackerMap.entrySet()) {
                                    // 清除离线超过一定时间的信息
                                    if (SystemClock.now() - entry.getValue() > 2 * Constants.DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
                                        offlineTaskTrackerMap.remove(entry.getKey());
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Check offline channel error!", t);
                        }
                    }
                }, 1, 1, TimeUnit.MINUTES);     // 1分钟检查一次

            }
            LOGGER.info("Start channel manager success!");
        } catch (Throwable t) {
            LOGGER.error("Start channel manager failed!", t);
        }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                channelCheckExecutorService.shutdown();
                offlineTaskTrackerScheduledFuture.cancel(true);
                offlineTaskTrackerCheckExecutorService.shutdown();
            }
            LOGGER.info("Stop channel manager success!");
        } catch (Throwable t) {
            LOGGER.error("Stop channel manager failed!", t);
        }
    }

    /**
     * 检查 关闭的channel
     */
    private void checkCloseChannel(NodeType nodeType, ConcurrentHashMap<String, List<ChannelWrapper>> channelMap) {
        for (Map.Entry<String, List<ChannelWrapper>> entry : channelMap.entrySet()) {
            List<ChannelWrapper> channels = entry.getValue();
            List<ChannelWrapper> removeList = new ArrayList<ChannelWrapper>();
            for (ChannelWrapper channel : channels) {
                if (channel.isClosed()) {
                    removeList.add(channel);
                    LOGGER.info("close channel={}", channel);
                }
            }
            channels.removeAll(removeList);
            // 加入到离线列表中
            if (nodeType == NodeType.TASK_TRACKER) {
                for (ChannelWrapper channelWrapper : removeList) {
                    offlineTaskTrackerMap.put(channelWrapper.getIdentity(), SystemClock.now());
                }
            }
        }
    }

    public List<ChannelWrapper> getChannels(String nodeGroup, NodeType nodeType) {
        if (nodeType == NodeType.JOB_CLIENT) {
            return clientChannelMap.get(nodeGroup);
        } else if (nodeType == NodeType.TASK_TRACKER) {
            return taskTrackerChannelMap.get(nodeGroup);
        }
        return null;
    }

    /**
     * 根据 节点唯一编号得到 channel
     */
    public ChannelWrapper getChannel(String nodeGroup, NodeType nodeType, String identity) {
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
     */
    public void offerChannel(ChannelWrapper channel) {
        String nodeGroup = channel.getNodeGroup();
        NodeType nodeType = channel.getNodeType();
        List<ChannelWrapper> channels = getChannels(nodeGroup, nodeType);
        if (channels == null) {
            channels = new ArrayList<ChannelWrapper>();
            if (nodeType == NodeType.JOB_CLIENT) {
                clientChannelMap.put(nodeGroup, channels);
            } else if (nodeType == NodeType.TASK_TRACKER) {
                taskTrackerChannelMap.put(nodeGroup, channels);
                // 如果在离线列表中，那么移除
                if (offlineTaskTrackerMap.containsKey(channel.getIdentity())) {
                    offlineTaskTrackerMap.remove(channel.getIdentity());
                }
            }
            channels.add(channel);
            LOGGER.info("new connected channel={}", channel);
        } else {
            if (!channels.contains(channel)) {
                channels.add(channel);
                LOGGER.info("new connected channel={}", channel);
            }
        }
    }

    public Long getOfflineTimestamp(String identity) {
        return offlineTaskTrackerMap.get(identity);
    }

    public void removeChannel(ChannelWrapper channel) {
        String nodeGroup = channel.getNodeGroup();
        NodeType nodeType = channel.getNodeType();
        List<ChannelWrapper> channels = getChannels(nodeGroup, nodeType);
        if (channels != null) {
            channels.remove(channel);
            LOGGER.info("remove channel={}", channel);
        }
    }
}
