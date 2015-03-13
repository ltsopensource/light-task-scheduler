package com.lts.job.tracker.support;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.support.Application;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.channel.ChannelWrapper;
import com.lts.job.tracker.domain.JobClientNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         客户端节点管理
 */
public class JobClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobClientManager.class);

    private final ConcurrentHashMap<String/*nodeGroup*/, ConcurrentHashSet<JobClientNode>> NODE_MAP = new ConcurrentHashMap<String, ConcurrentHashSet<JobClientNode>>();

    private ChannelManager channelManager;
    public JobClientManager(ChannelManager channelManager) {
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
        ConcurrentHashSet<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());

        synchronized (NODE_MAP) {
            if (jobClientNodes == null) {
                jobClientNodes = new ConcurrentHashSet<JobClientNode>();
                NODE_MAP.put(node.getGroup(), jobClientNodes);
            }
        }

        JobClientNode jobClientNode = new JobClientNode(node.getGroup(), node.getIdentity(), channel);
        LOGGER.info("添加JobClient节点:{}", jobClientNode);
        jobClientNodes.add(jobClientNode);

    }

    /**
     * 删除节点
     *
     * @param node
     */
    public void removeNode(Node node) {
        ConcurrentHashSet<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());
        if (jobClientNodes != null && jobClientNodes.size() != 0) {
            for (JobClientNode jobClientNode : jobClientNodes) {
                if (node.getIdentity().equals(jobClientNode.getIdentity())) {
                    LOGGER.info("删除JobClient节点:{}", jobClientNode);
                    jobClientNodes.remove(jobClientNode);
                }
            }
        }
    }

    /**
     * 得到 可用的 客户端节点
     *
     * @param nodeGroup
     * @return
     */
    public JobClientNode getAvailableJobClient(String nodeGroup) {

        ConcurrentHashSet<JobClientNode> jobClientNodes = NODE_MAP.get(nodeGroup);

        if (jobClientNodes == null || jobClientNodes.size() == 0) {
            return null;
        }

        int size = jobClientNodes.size();
        int index = getRandomIndex(size);

        List<JobClientNode> list = new ArrayList<JobClientNode>(jobClientNodes);

        JobClientNode jobClientNode = null;
        int retry = 0;
        while (jobClientNode == null && retry < size) {
            jobClientNode = list.get(index);
            // 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = channelManager.getChannel(jobClientNode.getNodeGroup(), NodeType.CLIENT, jobClientNode.getIdentity());
                if (channel != null) {
                    // 更新channel
                    jobClientNode.setChannel(channel);
                } else {
                    jobClientNode = null;
                }
            }
            index = (index + 1) % size;
            retry++;
        }

        return jobClientNode;
    }

    private int getRandomIndex(int size) {
        int min = 1;
        int max = size;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextInt(max) % (max - min + 1) + min - 1;
    }

}
