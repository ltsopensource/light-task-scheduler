package com.github.ltsopensource.jobtracker.domain;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 * Job Tracker 节点
 */
public class JobTrackerNode extends Node {

    public JobTrackerNode() {
        this.setNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.TASK_TRACKER);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
