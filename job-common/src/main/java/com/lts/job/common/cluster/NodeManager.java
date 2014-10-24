package com.lts.job.common.cluster;


import com.lts.job.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 * 节点管理 (主要用于管理自己关注的节点)
 */
public class NodeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    private static final ConcurrentHashMap<NodeType, List<Node>> NODES = new ConcurrentHashMap<NodeType, List<Node>>();

    public static void addNode(Node node) {
        List<Node> nodeList = NODES.get(node.getNodeType());
        if (CollectionUtils.isEmpty(nodeList)) {
            nodeList = new CopyOnWriteArrayList<Node>();
            NODES.put(node.getNodeType(), nodeList);
        }
        nodeList.add(node);
        LOGGER.info("添加节点" + node);
    }

    public static List<Node> getNodeList(NodeType nodeType) {
        return NODES.get(nodeType);
    }

    public static void removeNode(Node node) {
        List<Node> nodeList = NODES.get(node.getNodeType());
        if(nodeList.remove(node)){
            LOGGER.info("删除节点" + node);
        }
    }

    public static void destroy() {
        NODES.clear();
    }
}
