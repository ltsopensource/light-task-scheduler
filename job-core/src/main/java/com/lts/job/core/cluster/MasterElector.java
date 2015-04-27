package com.lts.job.core.cluster;

import com.lts.job.core.Application;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.core.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 *         Master 选举
 *         选举思想:
 *         选出每种节点中得master, 通过每个节点的创建时间来决定 （创建时间最小的便是master, 即最早创建的是master）
 *         当master 挂掉之后, 要重新选举
 */
public class MasterElector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterElector.class);

    private Application application;
    private List<MasterNodeChangeListener> masterNodeChangeListenerList;
    private volatile Node master;

    public MasterElector(Application application) {
        this.application = application;
    }

    public void addMasterNodeChangeListener(MasterNodeChangeListener masterNodeChangeListener) {
        if (masterNodeChangeListenerList == null) {
            masterNodeChangeListenerList = new ArrayList<MasterNodeChangeListener>();
        }
        masterNodeChangeListenerList.add(masterNodeChangeListener);
    }

    public Node getMaster() {
        return master;
    }

    public void addNodes(List<Node> nodes) {
        Node newMaster = null;
        for (Node node : nodes) {
            if (newMaster == null) {
                newMaster = node;
            } else {
                if (newMaster.getCreateTime() > node.getCreateTime()) {
                    newMaster = node;
                }
            }
        }
        addNode(newMaster);
    }

    public void addNode(Node newNode) {
        if (master == null) {
            master = newNode;
            notifyListener();
        } else {
            if (master.getCreateTime() > newNode.getCreateTime()) {
                master = newNode;
                notifyListener();
            }
        }
    }

    public void removeNode(Node removedNode) {

        if (master != null) {
            if (master.getIdentity().equals(removedNode.getIdentity())) {
                // 如果挂掉的是master, 需要重新选举
                List<Node> nodes = application.getNodeManager().getNodeList(application.getConfig().getNodeType(), application.getConfig().getNodeGroup());
                if (CollectionUtils.isNotEmpty(nodes)) {
                    Node newMaster = null;
                    for (Node node : nodes) {
                        if (newMaster == null) {
                            newMaster = node;
                        } else {
                            if (newMaster.getCreateTime() > node.getCreateTime()) {
                                newMaster = node;
                            }
                        }
                    }
                    master = newMaster;
                    notifyListener();
                }
            }
        }
    }

    private void notifyListener() {
        boolean isMaster = false;
        if (application.getConfig().getIdentity().equals(master.getIdentity())) {
            LOGGER.info("Master节点变化为当前节点:{}", master.getPath());
            isMaster = true;
        } else {
            LOGGER.info("Master节点为:{}", master.getPath());
            isMaster = false;
        }

        if (masterNodeChangeListenerList != null) {
            for (MasterNodeChangeListener masterNodeChangeListener : masterNodeChangeListenerList) {
                try {
                    masterNodeChangeListener.change(master, isMaster);
                } catch (Throwable t) {
                    LOGGER.error("masterNodeChangeListener通知失败!", t);
                }
            }
        }
    }

}
