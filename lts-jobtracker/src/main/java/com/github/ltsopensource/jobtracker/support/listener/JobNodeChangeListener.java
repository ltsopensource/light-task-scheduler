package com.github.ltsopensource.jobtracker.support.listener;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.listener.NodeChangeListener;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         节点变化监听器
 */
public class JobNodeChangeListener implements NodeChangeListener {

    private JobTrackerAppContext appContext;

    public JobNodeChangeListener(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                appContext.getTaskTrackerManager().addNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                appContext.getJobClientManager().addNode(node);
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
                appContext.getTaskTrackerManager().removeNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                appContext.getJobClientManager().removeNode(node);
            }
        }
    }
}
