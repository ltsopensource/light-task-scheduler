package com.github.ltsopensource.admin.cluster;

import com.github.ltsopensource.admin.access.domain.NodeOnOfflineLog;
import com.github.ltsopensource.admin.request.NodePaginationReq;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.registry.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BackendRegistrySrv {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendRegistrySrv.class);
    private BackendAppContext appContext;
    private Registry registry;
    private NotifyListener notifyListener;

    public BackendRegistrySrv(BackendAppContext appContext) {
        this.appContext = appContext;
    }

    private void subscribe() {

        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(appContext.getNode());
        }
        registry.subscribe(appContext.getNode(), notifyListener);
    }

    public void reSubscribe() {
        // 取消订阅
        registry.unsubscribe(appContext.getNode(), notifyListener);
        // 清空内存数据
        appContext.getNodeMemCacheAccess().clear();
        // 重新订阅
        subscribe();
    }

    public PaginationRsp<Node> getOnlineNodes(NodePaginationReq request) {
        return appContext.getNodeMemCacheAccess().pageSelect(request);
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
            log.setHttpCmdPort(node.getHttpCmdPort());

            logs.add(log);
        }

        appContext.getBackendNodeOnOfflineLogAccess().insert(logs);
    }

    public void start() throws Exception {

        registry = RegistryFactory.getRegistry(appContext);

        notifyListener = new NotifyListener() {
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
        };

        subscribe();
    }
}
