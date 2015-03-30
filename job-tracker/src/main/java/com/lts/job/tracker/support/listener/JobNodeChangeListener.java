package com.lts.job.tracker.support.listener;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.tracker.support.TaskTrackerManager;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         节点变化监听器
 */
public class JobNodeChangeListener implements NodeChangeListener {

    private JobTrackerApplication application;

    public JobNodeChangeListener(JobTrackerApplication application) {
        this.application = application;
        this.jobClientManager = application.getJobClientManager();
        this.taskTrackerManager = application.getTaskTrackerManager();
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
            application.getDeadJobChecker().fixedDeadLock(node);
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
