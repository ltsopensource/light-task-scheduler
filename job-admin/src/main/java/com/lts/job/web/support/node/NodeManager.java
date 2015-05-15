package com.lts.job.web.support.node;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;

import java.util.List;
import java.util.Map;

/**
 * Created by hugui on 5/11/15.
 */
public interface NodeManager {

    /**
     * 得到所有节点
     * @param cluster
     * @return
     */
    public Map<NodeType, List<Node>> getAllNodes(String cluster);

    /**
     * 启用节点
     * @param node
     */
    public void enableNode(Node node);

    /**
     * 禁用节点
     * @param node
     */
    public void disableNode(Node node);

    /**
     * 更改节点的工作线程
     * @param node
     */
    public void updateWorkThreads(Node node);
}
