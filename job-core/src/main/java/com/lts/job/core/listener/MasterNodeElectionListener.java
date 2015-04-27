package com.lts.job.core.listener;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 * 用来监听 自己类型 节点的变化,用来选举master
 */
public class MasterNodeElectionListener implements NodeChangeListener {

    private Application application;

    public MasterNodeElectionListener(Application application) {
        this.application = application;
    }

    @Override
    public void addNode(Node node) {
        if (isSameGroup(node)) {
            application.getMasterElector().addNode(node);
        }
    }

    @Override
    public void removeNode(Node node) {
        if (isSameGroup(node)) {
            application.getMasterElector().removeNode(node);
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if(groupNodes.size() > 0){
            application.getMasterElector().addNodes(groupNodes);
        }
    }

    /**
     * 是否和当前节点是相同的GROUP
     * @param node
     * @return
     */
    private boolean isSameGroup(Node node){
        return node.getNodeType().equals(application.getConfig().getNodeType())
                && node.getGroup().equals(application.getConfig().getNodeGroup());
    }
}
