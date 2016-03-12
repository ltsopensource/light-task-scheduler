package com.lts.admin.cluster;

import com.lts.admin.access.domain.NodeOnOfflineLog;
import com.lts.admin.request.NodePaginationReq;
import com.lts.core.cluster.Node;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.registry.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BackendRegistryService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendRegistryService.class);
    @Autowired
    private BackendAppContext appContext;

    private Registry registry;

    private void register() {

        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(appContext.getNode());
        }
        registry.subscribe(appContext.getNode(), new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        appContext.getNodeMemCacheAccess().addNode(nodes);
                        LOGGER.info("ADD NODE " + nodes);
                        break;
                    case REMOVE:
                        appContext.getNodeMemCacheAccess().removeNode(nodes);
                        LOGGER.info("REMOVE NODE " + nodes);
                        break;
                }
                // 记录日志
                addLog(event, nodes);
            }
        });
    }

    public List<Node> getOnlineNodes(NodePaginationReq request) {
        return appContext.getNodeMemCacheAccess().search(request);
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

        appContext.getBackendNodeOnOfflineLogAccess().insert(logs);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        registry = RegistryFactory.getRegistry(appContext);

        register();
    }
}
