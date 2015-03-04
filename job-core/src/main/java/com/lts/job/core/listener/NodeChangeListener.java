package com.lts.job.core.listener;

import com.lts.job.core.cluster.Node;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * 节点变化监听器
 */
public interface NodeChangeListener {

    /**
     * 添加节点
     *
     * @param node
     */
    public void addNode(Node node);

    /**
     * 删除节点
     *
     * @param node
     */
    public void removeNode(Node node);

    /**
     * 批量添加节点
     * @param nodes
     */
    public void addNodes(List<Node> nodes);
}
