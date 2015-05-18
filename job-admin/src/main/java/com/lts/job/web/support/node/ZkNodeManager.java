package com.lts.job.web.support.node;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.registry.NodeRegistryUtils;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.zookeeper.ZookeeperClient;
import com.lts.job.zookeeper.zkclient.ZkClientZookeeperClient;
import com.lts.job.web.support.AppConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 5/11/15.
 */
public class ZkNodeManager implements NodeManager {

    private ZookeeperClient zkClient;

    public ZkNodeManager() {
        this.zkClient = new ZkClientZookeeperClient(AppConfigurer.getProperties("zookeeper.address"));
    }

    @Override
    public Map<NodeType, List<Node>> getAllNodes(String clusterName) {
        String basePath = NodeRegistryUtils.getRootPath(clusterName);
        List<String> nodeTypes = zkClient.getChildren(basePath);
        Map<NodeType, List<Node>> nodeMap = new HashMap<NodeType, List<Node>>();
        if (CollectionUtils.isNotEmpty(nodeTypes)) {
            for (String nodeType : nodeTypes) {
                List<String> nodes = zkClient.getChildren(basePath + "/" + nodeType);
                if (CollectionUtils.isEmpty(nodes)) {
                    List<Node> nodeList = new ArrayList<Node>(nodes.size());
                    for (String node : nodes) {
                        nodeList.add(NodeRegistryUtils.parse(node));
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

    public static void main(String[] args) {


    }
}
