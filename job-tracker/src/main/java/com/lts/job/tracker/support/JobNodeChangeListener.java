package com.lts.job.tracker.support;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.support.Application;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         节点变化监听器
 */
public class JobNodeChangeListener implements NodeChangeListener {

    private Application application;

    public JobNodeChangeListener(Application application) {
        this.application = application;
        this.jobClientManager = application.getAttribute(Constants.JOB_CLIENT_MANAGER);
        this.taskTrackerManager = application.getAttribute(Constants.TASK_TRACKER_MANAGER);
    }

    private TaskTrackerManager taskTrackerManager;
    private JobClientManager jobClientManager;

    @Override
    public void addNode(Node node) {
        if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
            taskTrackerManager.addNode(node);
        } else if (node.getNodeType().equals(NodeType.CLIENT)) {
            jobClientManager.addNode(node);
        }
    }

    @Override
    public void removeNode(Node node) {
        if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
            taskTrackerManager.removeNode(node);
            ((DeadJobChecker) application.getAttribute(Constants.DEAD_JOB_CHECKER)).fixedDeadLock(node);
        } else if (node.getNodeType().equals(NodeType.CLIENT)) {
            jobClientManager.removeNode(node);
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        for (Node node : nodes) {
            addNode(node);
        }
    }
}
