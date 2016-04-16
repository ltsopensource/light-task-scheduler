package com.github.ltsopensource.core.cluster;


import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.ListUtils;
import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.listener.NodeChangeListener;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.ec.EventInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         节点管理 (主要用于管理自己关注的节点)
 */
public class SubscribedNodeManager implements NodeChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribedNodeManager.class);
    private final ConcurrentHashMap<NodeType, Set<Node>> NODES = new ConcurrentHashMap<NodeType, Set<Node>>();

    private AppContext appContext;

    public SubscribedNodeManager(AppContext appContext) {
        this.appContext = appContext;
    }

    /**
     * 添加监听的节点
     */
    private void addNode(Node node) {
        _addNode(node);
//        if ((NodeType.JOB_TRACKER.equals(node.getNodeType()))) {
//            // 如果增加的JobTracker节点，那么直接添加，因为所有节点都需要监听
//            _addNode(node);
//        } else if (NodeType.JOB_TRACKER.equals(appContext.getConfig().getNodeType())) {
//            // 如果当天节点是JobTracker节点，那么直接添加，因为JobTracker节点要监听三种节点
//            _addNode(node);
//        } else if (appContext.getConfig().getNodeType().equals(node.getNodeType())
//                && appContext.getConfig().getNodeGroup().equals(node.getGroup())) {
//            // 剩下这种情况是JobClient和TaskTracker都只监听和自己同一个group的节点
//            _addNode(node);
//        }
    }

    private void _addNode(Node node) {
        Set<Node> nodeSet = NODES.get(node.getNodeType());
        if (CollectionUtils.isEmpty(nodeSet)) {
            nodeSet = new ConcurrentHashSet<Node>();
            Set<Node> oldNodeList = NODES.putIfAbsent(node.getNodeType(), nodeSet);
            if (oldNodeList != null) {
                nodeSet = oldNodeList;
            }
        }
        nodeSet.add(node);
        EventInfo eventInfo = new EventInfo(EcTopic.NODE_ADD);
        eventInfo.setParam("node", node);
        appContext.getEventCenter().publishSync(eventInfo);
        LOGGER.info("Add {}", node);
    }

    public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {

        Set<Node> nodes = NODES.get(nodeType);

        return ListUtils.filter(CollectionUtils.setToList(nodes), new ListUtils.Filter<Node>() {
            @Override
            public boolean filter(Node node) {
                return node.getGroup().equals(nodeGroup);
            }
        });
    }

    public List<Node> getNodeList(NodeType nodeType) {
        return CollectionUtils.setToList(NODES.get(nodeType));
    }

    private void removeNode(Node delNode) {
        Set<Node> nodeSet = NODES.get(delNode.getNodeType());

        if (CollectionUtils.isNotEmpty(nodeSet)) {
            for (Node node : nodeSet) {
                if (node.getIdentity().equals(delNode.getIdentity())) {
                    nodeSet.remove(node);
                    EventInfo eventInfo = new EventInfo(EcTopic.NODE_REMOVE);
                    eventInfo.setParam("node", node);
                    appContext.getEventCenter().publishSync(eventInfo);
                    LOGGER.info("Remove {}", node);
                }
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            addNode(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            removeNode(node);
        }
    }
}
