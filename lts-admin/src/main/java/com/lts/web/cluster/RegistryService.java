package com.lts.web.cluster;

import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.registry.NotifyEvent;
import com.lts.core.registry.NotifyListener;
import com.lts.core.registry.Registry;
import com.lts.core.registry.RegistryFactory;
import com.lts.web.repository.domain.NodeOnOfflineLog;
import com.lts.web.repository.mapper.NodeOnOfflineLogRepo;
import com.lts.web.repository.memory.NodeMemoryDatabase;
import com.lts.web.request.NodeRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
@Component
public class RegistryService implements InitializingBean {

    @Autowired
    NodeMemoryDatabase nodeMemoryDatabase;
    @Autowired
    AdminApplication application;
    @Autowired
    NodeOnOfflineLogRepo nodeOnOfflineLogRepo;

    private Registry registry;

    private void register() {

        registry.subscribe(application.getNode(), new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        nodeMemoryDatabase.addNode(nodes);
                        break;
                    case REMOVE:
                        nodeMemoryDatabase.removeNode(nodes);
                        break;
                }
                // 记录日志
                addLog(event, nodes);
            }
        });
    }

    public List<Node> getOnlineNodes(NodeRequest request) {
        return nodeMemoryDatabase.search(request);
    }

    /**
     * 记录节点上下线日志
     */
    private void addLog(NotifyEvent event, List<Node> nodes) {
        List<NodeOnOfflineLog> logs = new ArrayList<NodeOnOfflineLog>(nodes.size());

        for (Node node : nodes) {
            NodeOnOfflineLog log = new NodeOnOfflineLog();
            log.setLogTime(new Date());
            log.setEvent(event == NotifyEvent.ADD ? "ONLINE" : "OFFLINE");

            log.setClusterName(node.getClusterName());
            log.setCreateTime(node.getCreateTime());
            log.setGroup(node.getGroup());
            log.setHostName(node.getHostName());
            log.setIdentity(node.getIdentity());
            log.setIp(node.getIp());
            log.setPort(node.getPort());
            log.setThreads(node.getThreads());
            log.setNodeType(node.getNodeType());

            logs.add(log);
        }

        nodeOnOfflineLogRepo.insert(logs);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        registry = RegistryFactory.getRegistry(application);

        register();
    }
}
