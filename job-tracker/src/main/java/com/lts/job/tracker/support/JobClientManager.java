package com.lts.job.tracker.support;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.loadbalance.LoadBalance;
import com.lts.job.core.loadbalance.RandomLoadBalance;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.channel.ChannelWrapper;
import com.lts.job.tracker.domain.JobClientNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         客户端节点管理
 */
public class JobClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobClientManager.class);

    private final ConcurrentHashMap<String/*nodeGroup*/, ConcurrentHashSet<JobClientNode>> NODE_MAP = new ConcurrentHashMap<String, ConcurrentHashSet<JobClientNode>>();

    private LoadBalance loadBalance;
    private ChannelManager channelManager;

    public JobClientManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
        this.loadBalance = new RandomLoadBalance();
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

        if (CollectionUtils.isEmpty(jobClientNodes)) {
            return null;
        }

        List<JobClientNode> list = new ArrayList<JobClientNode>(jobClientNodes);

        while (list.size() > 0) {

            JobClientNode jobClientNode = loadBalance.select(list, null);

            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = channelManager.getChannel(jobClientNode.getNodeGroup(), NodeType.CLIENT, jobClientNode.getIdentity());
                if (channel != null) {
                    // 更新channel
                    jobClientNode.setChannel(channel);
                }
            }

            if (jobClientNode != null && jobClientNode.getChannel() != null && !jobClientNode.getChannel().isClosed()) {
                return jobClientNode;
            } else {
                list.remove(jobClientNode);
            }
        }
        return null;
    }

}
