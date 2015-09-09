package com.lts.jobtracker.support.listener;

import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.listener.NodeChangeListener;
import com.lts.jobtracker.domain.JobTrackerApplication;

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
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
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
//                application.getExecutingDeadJobChecker().fixedDeadNodeJob(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                application.getJobClientManager().removeNode(node);
            }
        }
    }
}
