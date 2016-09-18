package com.github.ltsopensource.jobtracker.support.cluster;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.loadbalance.LoadBalance;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.jobtracker.channel.ChannelWrapper;
import com.github.ltsopensource.jobtracker.domain.JobClientNode;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         客户端节点管理
 */
public class JobClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobClientManager.class);

    private final ConcurrentHashMap<String/*nodeGroup*/, Set<JobClientNode>> NODE_MAP = new ConcurrentHashMap<String, Set<JobClientNode>>();

    private LoadBalance loadBalance;
    private JobTrackerAppContext appContext;

    public JobClientManager(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig(), ExtConfig.JOB_CLIENT_SELECT_LOADBALANCE);
    }

    /**
     * get all connected node group
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    /**
     * 添加节点
     */
    public void addNode(Node node) {
        //  channel 可能为 null
        ChannelWrapper channel = appContext.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());

        if (jobClientNodes == null) {
            jobClientNodes = new ConcurrentHashSet<JobClientNode>();
            Set<JobClientNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), jobClientNodes);
            if (oldSet != null) {
                jobClientNodes = oldSet;
            }
        }

        JobClientNode jobClientNode = new JobClientNode(node.getGroup(), node.getIdentity(), channel);
        LOGGER.info("add JobClient node:{}", jobClientNode);
        jobClientNodes.add(jobClientNode);

        // create feedback queue
        appContext.getJobFeedbackQueue().createQueue(node.getGroup());
        appContext.getNodeGroupStore().addNodeGroup(NodeType.JOB_CLIENT, node.getGroup());
    }

    /**
     * 删除节点
     */
    public void removeNode(Node node) {
        Set<JobClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());
        if (jobClientNodes != null && jobClientNodes.size() != 0) {
            for (JobClientNode jobClientNode : jobClientNodes) {
                if (node.getIdentity().equals(jobClientNode.getIdentity())) {
                    LOGGER.info("remove JobClient node:{}", jobClientNode);
                    jobClientNodes.remove(jobClientNode);
                }
            }
        }
    }

    /**
     * 得到 可用的 客户端节点
     */
    public JobClientNode getAvailableJobClient(String nodeGroup) {

        Set<JobClientNode> jobClientNodes = NODE_MAP.get(nodeGroup);

        if (CollectionUtils.isEmpty(jobClientNodes)) {
            return null;
        }

        List<JobClientNode> list = new ArrayList<JobClientNode>(jobClientNodes);

        while (list.size() > 0) {

            JobClientNode jobClientNode = loadBalance.select(list, null);

            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = appContext.getChannelManager().getChannel(jobClientNode.getNodeGroup(), NodeType.JOB_CLIENT, jobClientNode.getIdentity());
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
