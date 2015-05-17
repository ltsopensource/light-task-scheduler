package com.lts.job.tracker.support.listener;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.tracker.domain.JobTrackerApplication;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         节点变化监听器
 */
public class JobNodeChangeListener implements NodeChangeListener {

    private JobTrackerApplication application;

    public JobNodeChangeListener(JobTrackerApplication application) {
        this.application = application;
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                application.getTaskTrackerManager().addNode(node);
            } else if (node.getNodeType().equals(NodeType.CLIENT)) {
                application.getJobClientManager().addNode(node);
            }
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                application.getTaskTrackerManager().removeNode(node);
                application.getDeadJobChecker().fixedDeadLock(node);
            } else if (node.getNodeType().equals(NodeType.CLIENT)) {
                application.getJobClientManager().removeNode(node);
            }
        }
    }
}
