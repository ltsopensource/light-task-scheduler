package com.lts.job.web.cluster;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.Node;
import com.lts.job.core.registry.NotifyEvent;
import com.lts.job.core.registry.NotifyListener;
import com.lts.job.core.registry.Registry;
import com.lts.job.core.registry.RegistryFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.ConcurrentHashSet;
import com.lts.job.core.util.StringUtils;
import com.lts.job.web.request.NodeRequest;
import com.lts.job.web.support.AppConfigurer;
import com.lts.job.web.support.memorydb.NodeMemDB;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class RegistryService implements InitializingBean {

    private final ConcurrentHashSet<String/*clusterName*/> MAP = new ConcurrentHashSet<String>();
    private NodeMemDB nodeMemDB = new NodeMemDB();

    @Autowired
    @Qualifier("application")
    AdminApplication application;

    public synchronized void register(String clusterName) {

        if (MAP.contains(clusterName)) {
            return;
        }

        Config config = application.getConfig();
        config.setClusterName(clusterName);

        Registry registry = RegistryFactory.getRegistry(config);

        registry.subscribe(application.getNode(), new NotifyListener() {
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

        MAP.add(clusterName);
    }

    public List<String> getAllClusterNames() {
        return new ArrayList<String>(MAP.list());
    }

    public List<Node> getNodes(NodeRequest request) {
        return nodeMemDB.search(request);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String clusterNames = AppConfigurer.getProperties("clusterNames");
        if (StringUtils.isNotEmpty(clusterNames)) {
            String[] clusters = clusterNames.split(",");
            for (String cluster : clusters) {
                register(cluster.trim());
            }
        }
    }
}
