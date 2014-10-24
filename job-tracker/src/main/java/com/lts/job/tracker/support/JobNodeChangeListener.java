package com.lts.job.tracker.support;

import com.lts.job.common.cluster.Node;
import com.lts.job.common.cluster.NodeType;
import com.lts.job.common.listener.NodeChangeListener;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * 节点变化监听器
 */
public class JobNodeChangeListener implements NodeChangeListener {

    @Override
    public void addNode(Node node) {
        if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
            TaskTrackerManager.INSTANCE.addNode(node);
        }else if(node.getNodeType().equals(NodeType.CLIENT)){
            JobClientManager.INSTANCE.addNode(node);
        }
    }

    @Override
    public void removeNode(Node node) {
        if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
            TaskTrackerManager.INSTANCE.removeNode(node);
            DeadJobChecker.fixedDeadLock(node);
        }else if(node.getNodeType().equals(NodeType.CLIENT)){
            JobClientManager.INSTANCE.removeNode(node);
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        for (Node node : nodes) {
            addNode(node);
        }
    }
}
