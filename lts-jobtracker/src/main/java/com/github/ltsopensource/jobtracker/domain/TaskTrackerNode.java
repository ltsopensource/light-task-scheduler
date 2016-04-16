package com.github.ltsopensource.jobtracker.domain;


import com.github.ltsopensource.jobtracker.channel.ChannelWrapper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskTracker状态对象
 */
public class TaskTrackerNode {
    // 节点组名称
    public String nodeGroup;
    // 可用线程数
    public AtomicInteger availableThread;
    // 唯一标识
    public String identity;
    // 该节点的channel
    public ChannelWrapper channel;

    public Long timestamp = null;

    public TaskTrackerNode(String nodeGroup, int availableThread, String identity, ChannelWrapper channel) {
        this.nodeGroup = nodeGroup;
        this.availableThread = new AtomicInteger(availableThread);
        this.identity = identity;
        this.channel = channel;
    }

    public TaskTrackerNode(String identity) {
        this.identity = identity;
    }

    public TaskTrackerNode(String identity, String nodeGroup) {
        this.nodeGroup = nodeGroup;
        this.identity = identity;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public AtomicInteger getAvailableThread() {
        return availableThread;
    }

    public void setAvailableThread(int availableThread) {
        this.availableThread = new AtomicInteger(availableThread);
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public ChannelWrapper getChannel() {
        return channel;
    }

    public void setChannel(ChannelWrapper channel) {
        this.channel = channel;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskTrackerNode)) return false;

        TaskTrackerNode that = (TaskTrackerNode) o;

        if (identity != null ? !identity.equals(that.identity) : that.identity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TaskTrackerNode{" +
                "nodeGroup='" + nodeGroup + '\'' +
                ", availableThread=" + (availableThread == null ? 0 : availableThread.get()) +
                ", identity='" + identity + '\'' +
                ", channel=" + channel +
                '}';
    }
}