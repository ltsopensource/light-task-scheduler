package com.lts.job.client.domain;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 * 任务客户端节点
 */
public class JobClientNode extends Node {

    public JobClientNode() {
        this.setNodeType(NodeType.CLIENT);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.CLIENT);
    }

}
