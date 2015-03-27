package com.lts.job.core.cluster;


import com.lts.job.core.Application;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         节点管理 (主要用于管理自己关注的节点)
 */
public class NodeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    private final ConcurrentHashMap<NodeType, List<Node>> NODES = new ConcurrentHashMap<NodeType, List<Node>>();

    private Application application;

    public NodeManager(Application application) {
        this.application = application;
    }

    public void addNode(Node node) {

        // JobClient 和 TaskTracker 只对自己同一个组的节点关注和JobTracker节点关注
        // JobTracker 要对所有节点都要关注
        if ((NodeType.JOB_TRACKER.equals(node.getNodeType())) ||
                ((application.getConfig().getNodeType().equals(node.getNodeType())
                        && application.getConfig().getNodeGroup().equals(node.getGroup()))
                        || (NodeType.JOB_TRACKER.equals(application.getConfig().getNodeType())))
                ) {

            List<Node> nodeList = NODES.get(node.getNodeType());
            if (CollectionUtils.isEmpty(nodeList)) {
                nodeList = new CopyOnWriteArrayList<Node>();
                NODES.put(node.getNodeType(), nodeList);
            }
            nodeList.add(node);
            LOGGER.info("添加节点{}", node);
        }
    }

    public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {

        List<Node> nodes = NODES.get(nodeType);

        return ListUtils.filter(nodes, new ListUtils.Filter<Node>() {
            @Override
            public boolean filter(Node node) {
                return node.getGroup().equals(nodeGroup);
            }
        });
    }

    public List<Node> getNodeList(NodeType nodeType) {
        return NODES.get(nodeType);
    }

    public void removeNode(Node node) {
        List<Node> nodeList = NODES.get(node.getNodeType());
        if (nodeList.remove(node)) {
            LOGGER.info("删除节点{}", node);
        }
    }

    /**
     * 是否包含某个元素
     * @param node
     * @return
     */
    public boolean contains(Node node) {
        List<Node> nodes = NODES.get(node.getNodeType());
        for (Node inNode : nodes) {
            if (inNode.getIdentity().equals(node.getIdentity())) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        NODES.clear();
    }
}
