package com.lts.admin.cluster;

import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public class BackendNode extends Node {

    public BackendNode() {
        this.setNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.TASK_TRACKER);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
