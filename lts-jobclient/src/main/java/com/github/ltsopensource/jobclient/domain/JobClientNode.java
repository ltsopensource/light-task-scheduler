package com.github.ltsopensource.jobclient.domain;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         任务客户端节点
 */
public class JobClientNode extends Node {

    public JobClientNode() {
        this.setNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.MONITOR);
    }

}
