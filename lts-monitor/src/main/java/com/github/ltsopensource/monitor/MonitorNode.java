package com.github.ltsopensource.monitor;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorNode extends Node {

    public MonitorNode() {
        this.setNodeType(NodeType.MONITOR);
    }
}
