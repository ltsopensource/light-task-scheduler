package com.lts.job.web.cluster;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.cluster.SubscribedNodeManager;
import com.lts.job.core.registry.NotifyEvent;
import com.lts.job.core.registry.NotifyListener;
import com.lts.job.core.registry.Registry;
import com.lts.job.core.registry.RegistryFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.Holder;
import com.lts.job.core.util.StringUtils;
import com.lts.job.web.support.AppConfigurer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class RegistryService {

    private final ConcurrentHashMap<String/*clusterName*/, SubscribedNodeManager> NODE_MANAGER_MAP;

    public RegistryService() {
        this.NODE_MANAGER_MAP = new ConcurrentHashMap<String, SubscribedNodeManager>();
    }

    public synchronized void register(String clusterName) {
        SubscribedNodeManager subscribedNodeManager = NODE_MANAGER_MAP.get(clusterName);
        if (subscribedNodeManager != null) {
            //  already registered
            return;
        }

        final Node node = new Node();
        node.setIdentity("LTS_admin_" + StringUtils.generateUUID());
        node.addListenNodeType(NodeType.JOB_CLIENT);
        node.addListenNodeType(NodeType.TASK_TRACKER);
        node.addListenNodeType(NodeType.JOB_TRACKER);
        node.setNodeType(NodeType.JOB_ADMIN);

        Config config = new Config();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperties("registry.address"));
        config.setClusterName(clusterName);

        Application application = new AdminApplication();
        application.setConfig(config);
        subscribedNodeManager = new SubscribedNodeManager(application);

        Registry registry = RegistryFactory.getRegistry(config);

        final Holder<SubscribedNodeManager> subscribedNodeManagerHolder =
                new Holder<SubscribedNodeManager>(subscribedNodeManager);

        registry.subscribe(node, new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        subscribedNodeManagerHolder.get().addNodes(nodes);
                        break;
                    case REMOVE:
                        subscribedNodeManagerHolder.get().removeNodes(nodes);
                        break;
                }
            }
        });

        NODE_MANAGER_MAP.put(clusterName, subscribedNodeManagerHolder.get());
    }

    public List<Node> getAllNodes(String clusterName) {
        SubscribedNodeManager subscribedNodeManager = NODE_MANAGER_MAP.get(clusterName);
        if (subscribedNodeManager == null) {
            return new ArrayList<Node>(0);
        }
        return subscribedNodeManager.getNodeList();
    }

    public List<Node> getAllNodes() {
        List<Node> result = new ArrayList<Node>();
        Collection<SubscribedNodeManager> subscribedNodeManagers = NODE_MANAGER_MAP.values();
        if (CollectionUtils.isNotEmpty(subscribedNodeManagers)) {
            for (SubscribedNodeManager subscribedNodeManager : subscribedNodeManagers) {
                List<Node> nodes = subscribedNodeManager.getNodeList();
                if (CollectionUtils.isNotEmpty(nodes)) {
                    result.addAll(nodes);
                }
            }
        }
        return result;
    }
}
