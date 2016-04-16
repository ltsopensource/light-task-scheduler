package com.github.ltsopensource.tasktracker.domain;


import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         TaskTracker 节点
 */
public class TaskTrackerNode extends Node {

    public TaskTrackerNode() {
        this.setNodeType(NodeType.TASK_TRACKER);
        // 关注 JobTracker
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.TASK_TRACKER);
        this.addListenNodeType(NodeType.MONITOR);
    }

}
