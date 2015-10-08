package com.lts.core.registry.zookeeper;

import com.lts.core.Application;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.extension.ExtensionLoader;
import com.lts.core.registry.FailbackRegistry;
import com.lts.core.registry.NodeRegistryUtils;
import com.lts.core.registry.NotifyEvent;
import com.lts.core.registry.NotifyListener;
import com.lts.zookeeper.ChildListener;
import com.lts.zookeeper.StateListener;
import com.lts.zookeeper.ZookeeperClient;
import com.lts.zookeeper.ZookeeperTransporter;

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
    private final ConcurrentHashMap<String/*parentPath*/, List<String/*children*/>> cachedChildrenNodeMap;

    private final ConcurrentMap<Node, ConcurrentMap<NotifyListener, ChildListener>> zkListeners;

    private String clusterName;

    public ZookeeperRegistry(final Application application) {
        super(application);
        this.clusterName = application.getConfig().getClusterName();
        this.cachedChildrenNodeMap = new ConcurrentHashMap<String, List<String>>();
        ZookeeperTransporter zookeeperTransporter = ExtensionLoader.getExtensionLoader(ZookeeperTransporter.class).getAdaptiveExtension();
        this.zkClient = zookeeperTransporter.connect(application.getConfig());
        this.zkListeners = new ConcurrentHashMap<Node, ConcurrentMap<NotifyListener, ChildListener>>();
        // 默认是连成功的(用zkclient时候，第一次不会有state changed时间暴露给用户，
        // 他居然在new ZkClient的时候就直接连接了，给个提供listener的构造函数或者把启动改为start方法都ok呀，蛋疼)
        application.getRegistryStatMonitor().setAvailable(true);

        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == DISCONNECTED) {
                    application.getRegistryStatMonitor().setAvailable(false);
                } else if (state == CONNECTED) {
                    application.getRegistryStatMonitor().setAvailable(true);
                } else if (state == RECONNECTED) {
                    try {
                        application.getRegistryStatMonitor().setAvailable(true);
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
        if (zkClient.exists(node.toFullString())) {
            return;
        }
        zkClient.create(node.toFullString(), true, false);
    }

    @Override
    protected void doUnRegister(Node node) {
        zkClient.delete(node.toFullString());
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
            List<String> children = zkClient.addChildListener(listenNodePath, zkListener);

            if (CollectionUtils.isNotEmpty(children)) {
                List<Node> listenedNodes = new ArrayList<Node>();
                for (String child : children) {
                    Node listenedNode = NodeRegistryUtils.parse(listenNodePath + "/" + child);
                    listenedNodes.add(listenedNode);
                }
                notify(NotifyEvent.ADD, listenedNodes, listener);
                cachedChildrenNodeMap.put(listenNodePath, children);
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

                public void childChanged(String parentPath, List<String> currentChildren) {

                    if (CollectionUtils.isEmpty(currentChildren)) {
                        currentChildren = new ArrayList<String>(0);
                    }

                    List<String> oldChildren = cachedChildrenNodeMap.get(parentPath);
                    // 1. 找出增加的 节点
                    List<String> addChildren = CollectionUtils.getLeftDiff(currentChildren, oldChildren);
                    // 2. 找出减少的 节点
                    List<String> decChildren = CollectionUtils.getLeftDiff(oldChildren, currentChildren);

                    if (CollectionUtils.isNotEmpty(addChildren)) {

                        List<Node> nodes = new ArrayList<Node>(addChildren.size());
                        for (String child : addChildren) {
                            Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.ADD, nodes, listener);
                    }

                    if (CollectionUtils.isNotEmpty(decChildren)) {
                        List<Node> nodes = new ArrayList<Node>(addChildren.size());
                        for (String child : decChildren) {
                            Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
                            nodes.add(node);
                        }
                        ZookeeperRegistry.this.notify(NotifyEvent.REMOVE, nodes, listener);
                    }
                    cachedChildrenNodeMap.put(parentPath, currentChildren);
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

