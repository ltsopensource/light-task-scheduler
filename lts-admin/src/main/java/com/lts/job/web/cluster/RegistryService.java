package com.lts.job.web.cluster;

import com.lts.job.core.Application;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.registry.NotifyEvent;
import com.lts.job.core.registry.NotifyListener;
import com.lts.job.core.registry.Registry;
import com.lts.job.core.registry.RegistryFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.core.util.StringUtils;
import com.lts.job.web.request.NodeRequest;
import com.lts.job.web.support.AppConfigurer;
import com.lts.job.web.support.db.NodeMemDB;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class RegistryService {

    private final ConcurrentHashSet<String/*clusterName*/> map;

    private NodeMemDB nodeMemDB;

    public RegistryService() {
        this.map = new ConcurrentHashSet<String>();
        this.nodeMemDB = new NodeMemDB();
    }

    public synchronized void register(String clusterName) {

        if (map.contains(clusterName)) {
            return;
        }

        final Node node = new Node();
        node.setIdentity("LTS_admin_" + StringUtils.generateUUID());
        node.addListenNodeType(NodeType.JOB_CLIENT);
        node.addListenNodeType(NodeType.TASK_TRACKER);
        node.addListenNodeType(NodeType.JOB_TRACKER);

        Config config = new Config();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperties("registry.address"));
        config.setClusterName(clusterName);

        Application application = new AdminApplication();
        application.setConfig(config);

        Registry registry = RegistryFactory.getRegistry(config);

        registry.subscribe(node, new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        nodeMemDB.addNode(nodes);
                        break;
                    case REMOVE:
                        nodeMemDB.removeNode(nodes);
                        break;
                }
            }
        });

        map.add(clusterName);
    }

    public List<String> getAllClusterNames() {
        return new ArrayList<String>(map.list());
    }

    public List<Node> getNodes(NodeRequest request) {
        return nodeMemDB.search(request);
    }
}
