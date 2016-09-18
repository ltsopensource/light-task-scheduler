package com.github.ltsopensource.core.listener;

import com.github.ltsopensource.core.cluster.Node;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
public interface NodeChangeListener {

    /**
     * 添加节点
     *
     * @param nodes 节点列表
     */
    public void addNodes(List<Node> nodes);

    /**
     * 移除节点
     * @param nodes 节点列表
     */
    public void removeNodes(List<Node> nodes);

}
