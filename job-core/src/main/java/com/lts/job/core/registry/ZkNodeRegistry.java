package com.lts.job.core.registry;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.listener.NodeChangeListener;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.registry.zookeeper.ChildListener;
import com.lts.job.registry.zookeeper.StateListener;
import com.lts.job.registry.zookeeper.ZookeeperClient;
import com.lts.job.registry.zookeeper.zkclient.ZkClientZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 * 节点注册器，并监听自己关注的节点
 */
public class ZkNodeRegistry implements Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkNodeRegistry.class);
    private ZookeeperClient zkClient;
    // 用来记录父节点下的子节点的变化
    private final ConcurrentHashMap<String/*path*/, List<String/*children*/>> NODE_CHILDREN_MAP = new ConcurrentHashMap<String, List<String>>();
    private ChildChangeListener listener;
    private List<NodeChangeListener> nodeChangeListeners;
    private Application application;
    private ZkPathParser zkPathParser;

    public ZkNodeRegistry(Application application) {
        this.listener = new ChildChangeListener();
        this.application = application;
        this.zkPathParser = new ZkPathParser(application);
        application.setPathParser(this.zkPathParser);
    }

    /**
     * 添加节点变化监听器
     * @param nodeChangeListener
     */
    public void addNodeChangeListener(NodeChangeListener nodeChangeListener) {
        if (this.nodeChangeListeners == null) {
            this.nodeChangeListeners = new ArrayList<NodeChangeListener>();
        }
        this.nodeChangeListeners.add(nodeChangeListener);
    }

    @Override
    public void register(final Node node) {
        zkClient = new ZkClientZookeeperClient(application.getConfig().getZookeeperAddress());
        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        doSubscribe(node);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });
        doSubscribe(node);
    }

    protected void doSubscribe(Node node) {

        if (zkClient.exists(node.getPath())) {
            return;
        }

        zkClient.create(node.getPath(), true, false);

        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isNotEmpty(listenNodeTypes)) {

            for (NodeType nodeType : listenNodeTypes) {
                String listenNodePath = zkPathParser.getPath(nodeType);
                // 为自己关注的 节点 添加监听
                zkClient.addChildListener(listenNodePath, listener);

                // 将自己关注的 节点类型加入到节点管理中去
                List<String> children = zkClient.getChildren(listenNodePath);
                if (CollectionUtils.isNotEmpty(children)) {
                    List<Node> listenedNodes = new ArrayList<Node>();
                    for (String child : children) {
                        Node listenedNode = zkPathParser.parse(listenNodePath + "/" + child);
                        listenedNodes.add(listenedNode);
                        application.getNodeManager().addNode(listenedNode);
                    }
                    if (CollectionUtils.isNotEmpty(nodeChangeListeners)) {
                        for (NodeChangeListener nodeChangeListener : nodeChangeListeners) {
                            nodeChangeListener.addNodes(listenedNodes);
                        }
                    }
                    NODE_CHILDREN_MAP.put(listenNodePath, children);
                }
            }
        }
    }

    protected void doUnSubscribe(Node node) {
        zkClient.delete(node.getPath());

        List<NodeType> listenNodeTypes = node.getListenNodeTypes();
        if (CollectionUtils.isNotEmpty(listenNodeTypes)) {
            for (NodeType nodeType : listenNodeTypes) {
                zkClient.removeChildListener(zkPathParser.getPath(nodeType), listener);

                application.getNodeManager().destroy();
            }
        }
    }

    @Override
    public void unregister(Node node) {
        doUnSubscribe(node);
        zkClient.close();
    }

    @Override
    public void destroy() {
        try {
            zkClient.close();
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
    }

    private class ChildChangeListener implements ChildListener {

        @Override
        public void childChanged(String path, List<String> children) {

            List<String> oldChildren = NODE_CHILDREN_MAP.get(path);
            // 1. 找出增加的 节点
            List<String> addChildren = CollectionUtils.getLeftDiff(children, oldChildren);
            // 2. 找出减少的 节点
            List<String> decChildren = CollectionUtils.getLeftDiff(oldChildren, children);

            if (CollectionUtils.isNotEmpty(addChildren)) {
                for (String child : addChildren) {
                    Node node = zkPathParser.parse(path + "/" + child);

                    application.getNodeManager().addNode(node);
                    if (CollectionUtils.isNotEmpty(nodeChangeListeners)) {
                        for (NodeChangeListener nodeChangeListener : nodeChangeListeners) {
                            nodeChangeListener.addNode(node);
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(decChildren)) {
                for (String child : decChildren) {
                    Node node = zkPathParser.parse(path + "/" + child);
                    application.getNodeManager().removeNode(node);
                    if (CollectionUtils.isNotEmpty(nodeChangeListeners)) {
                        for (NodeChangeListener nodeChangeListener : nodeChangeListeners) {
                            nodeChangeListener.removeNode(node);
                        }
                    }
                }
            }
            NODE_CHILDREN_MAP.put(path, children);
        }
    }
}

