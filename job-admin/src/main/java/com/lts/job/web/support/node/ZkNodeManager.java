package com.lts.job.web.support.node;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.registry.ZkNodeUtils;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.registry.zookeeper.ZookeeperClient;
import com.lts.job.registry.zookeeper.zkclient.ZkClientZookeeperClient;
import com.lts.job.web.support.AppConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hugui on 5/11/15.
 */
public class ZkNodeManager implements NodeManager {

    private ZookeeperClient zkClient;

    public ZkNodeManager() {
        this.zkClient = new ZkClientZookeeperClient(AppConfigurer.getProperties("zookeeper.address"));
    }

    @Override
    public Map<NodeType, List<Node>> getAllNodes(String clusterName) {
        String basePath = ZkNodeUtils.getBasePath(clusterName);
        List<String> nodeTypes = zkClient.getChildren(basePath);
        Map<NodeType, List<Node>> nodeMap = new HashMap<NodeType, List<Node>>();
        if (CollectionUtils.isNotEmpty(nodeTypes)) {
            for (String nodeType : nodeTypes) {
                List<String> nodes = zkClient.getChildren(basePath + "/" + nodeType);
                if (CollectionUtils.isEmpty(nodes)) {
                    List<Node> nodeList = new ArrayList<Node>(nodes.size());
                    for (String node : nodes) {
                        nodeList.add(ZkNodeUtils.parse(clusterName, node));
                    }
                    nodeMap.put(NodeType.valueOf(nodeType), nodeList);
                }
            }
        }
        return nodeMap;
    }

    @Override
    public void enableNode(Node node) {

    }

    @Override
    public void disableNode(Node node) {

    }

    @Override
    public void updateWorkThreads(Node node) {

    }

}
