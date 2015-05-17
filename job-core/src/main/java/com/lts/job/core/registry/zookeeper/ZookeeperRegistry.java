package com.lts.job.core.registry.zookeeper;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.registry.FailbackRegistry;
import com.lts.job.core.registry.NodeRegistryUtils;
import com.lts.job.core.registry.NotifyEvent;
import com.lts.job.core.registry.NotifyListener;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.zookeeper.ChildListener;
import com.lts.job.zookeeper.StateListener;
import com.lts.job.zookeeper.ZookeeperClient;
import com.lts.job.zookeeper.zkclient.ZkClientZookeeperClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 *         节点注册器，并监听自己关注的节点
 */
public class ZookeeperRegistry extends FailbackRegistry {

    private ZookeeperClient zkClient;
    // 用来记录父节点下的子节点的变化
    private final ConcurrentHashMap<String/*parentPath*/, List<String/*children*/>> childrenNodeMap;

    private final ConcurrentMap<Node, ConcurrentMap<NotifyListener, ChildListener>> zkListeners;

    private String clusterName;

    public ZookeeperRegistry(Application application) {
        super(application);
        this.clusterName = application.getConfig().getClusterName();
        this.childrenNodeMap = new ConcurrentHashMap<String, List<String>>();

        String address = application.getConfig().getRegistryAddress().replace("zookeeper://", "");
        this.zkClient = new ZkClientZookeeperClient(address);
        this.zkListeners = new ConcurrentHashMap<Node, ConcurrentMap<NotifyListener, ChildListener>>();
        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        recover();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    protected void doRegister(Node node) {
        String fullPath = NodeRegistryUtils.getFullPath(clusterName, node);
        if (zkClient.exists(fullPath)) {
            return;
        }
        zkClient.create(fullPath, true, false);
    }

    @Override
    protected void doUnRegister(Node node) {
        String fullPath = NodeRegistryUtils.getFullPath(clusterName, node);
        zkClient.delete(fullPath);
    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {
        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isEmpty(listenNodeTypes)) {
            return;
        }
        for (NodeType listenNodeType : listenNodeTypes) {
            String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);

            ChildListener zkListener = addZkListener(node, listener);

            // 为自己关注的 节点 添加监听
            zkClient.addChildListener(listenNodePath, zkListener);

            // 将自己关注的 节点类型加入到节点管理中去
            List<String> children = zkClient.getChildren(listenNodePath);
            if (CollectionUtils.isNotEmpty(children)) {
                List<Node> listenedNodes = new ArrayList<Node>();
                for (String child : children) {
                    Node listenedNode = NodeRegistryUtils.parse(clusterName, listenNodePath + "/" + child);
                    listenedNodes.add(listenedNode);
                }
                notify(NotifyEvent.ADD, listenedNodes, listener);
                childrenNodeMap.put(listenNodePath, children);
            }
        }
    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            if (zkListener != null) {
                List<NodeType> listenNodeTypes = node.getListenNodeTypes();
                if (CollectionUtils.isEmpty(listenNodeTypes)) {
                    return;
                }
                for (NodeType listenNodeType : listenNodeTypes) {
                    String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);
                    zkClient.removeChildListener(listenNodePath, zkListener);
                }
            }
        }
    }


    private ChildListener addZkListener(Node node, final NotifyListener listener) {

        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
        if (listeners == null) {
            zkListeners.putIfAbsent(node, new ConcurrentHashMap<NotifyListener, ChildListener>());
            listeners = zkListeners.get(node);
        }
        ChildListener zkListener = listeners.get(listener);
        if (zkListener == null) {

            listeners.putIfAbsent(listener, new ChildListener() {

                public void childChanged(String parentPath, List<String> currentChilds) {
                    List<String> oldChilds = childrenNodeMap.get(parentPath);
                    // 1. 找出增加的 节点
                    List<String> addChilds = CollectionUtils.getLeftDiff(currentChilds, oldChilds);
                    // 2. 找出减少的 节点
                    List<String> decChilds = CollectionUtils.getLeftDiff(oldChilds, currentChilds);

                    if (CollectionUtils.isNotEmpty(addChilds)) {

                        List<Node> nodes = new ArrayList<Node>(addChilds.size());
                        for (String child : addChilds) {
                            Node node = NodeRegistryUtils.parse(clusterName, parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.ADD, nodes, listener);
                    }

                    if (CollectionUtils.isNotEmpty(decChilds)) {
                        List<Node> nodes = new ArrayList<Node>(addChilds.size());
                        for (String child : decChilds) {
                            Node node = NodeRegistryUtils.parse(clusterName, parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.REMOVE, nodes, listener);
                    }
                    childrenNodeMap.put(parentPath, currentChilds);
                }
            });
            zkListener = listeners.get(listener);
        }
        return zkListener;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close zookeeper client " + getNode() + ", cause: " + e.getMessage(), e);
        }
    }
}

