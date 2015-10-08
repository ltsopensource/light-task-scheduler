package com.lts.core.listener;

import com.lts.core.Application;
import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 *         用来监听 自己类型 节点的变化,用来选举master
 */
public class MasterElectionListener implements NodeChangeListener {

    private Application application;

    public MasterElectionListener(Application application) {
        this.application = application;
    }

    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if (groupNodes.size() > 0) {
            application.getMasterElector().removeNode(groupNodes);
        }
    }

    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if (groupNodes.size() > 0) {
            application.getMasterElector().addNodes(groupNodes);
        }
    }

    /**
     * 是否和当前节点是相同的GROUP
     *
     * @param node
     * @return
     */
    private boolean isSameGroup(Node node) {
        return node.getNodeType().equals(application.getConfig().getNodeType())
                && node.getGroup().equals(application.getConfig().getNodeGroup());
    }

}
