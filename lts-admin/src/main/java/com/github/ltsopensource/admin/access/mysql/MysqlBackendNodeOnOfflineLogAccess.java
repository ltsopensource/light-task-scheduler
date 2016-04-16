package com.github.ltsopensource.admin.access.mysql;

import com.github.ltsopensource.admin.access.RshHandler;
import com.github.ltsopensource.admin.access.domain.NodeOnOfflineLog;
import com.github.ltsopensource.admin.access.face.BackendNodeOnOfflineLogAccess;
import com.github.ltsopensource.admin.request.NodeOnOfflineLogPaginationReq;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.monitor.access.mysql.MysqlAbstractJdbcAccess;
import com.github.ltsopensource.store.jdbc.builder.*;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlBackendNodeOnOfflineLogAccess extends MysqlAbstractJdbcAccess implements BackendNodeOnOfflineLogAccess {

    public MysqlBackendNodeOnOfflineLogAccess(Config config) {
        super(config);
    }

    @Override
    protected String getTableName() {
        return "lts_admin_node_onoffline_log";
    }

    @Override
    public void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs) {
        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("log_time",
                        "event",
                        "node_type",
                        "cluster_name",
                        "ip",
                        "port",
                        "host_name",
                        "group",
                        "create_time",
                        "threads",
                        "identity",
                        "http_cmd_port");
        for (NodeOnOfflineLog nodeOnOfflineLog : nodeOnOfflineLogs) {
            insertSql.values(nodeOnOfflineLog.getLogTime(),
                    nodeOnOfflineLog.getEvent(),
                    nodeOnOfflineLog.getNodeType().name(),
                    nodeOnOfflineLog.getClusterName(),
                    nodeOnOfflineLog.getIp(),
                    nodeOnOfflineLog.getPort(),
                    nodeOnOfflineLog.getHostName(),
                    nodeOnOfflineLog.getGroup(),
                    nodeOnOfflineLog.getCreateTime(),
                    nodeOnOfflineLog.getThreads(),
                    nodeOnOfflineLog.getIdentity(),
                    nodeOnOfflineLog.getHttpCmdPort()
                    );
        }
        insertSql.doBatchInsert();
    }

    @Override
    public List<NodeOnOfflineLog> select(NodeOnOfflineLogPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .orderBy()
                .column("log_time", OrderByType.DESC)
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.NODE_ON_OFFLINE_LOG_LIST_RSH);
    }

    @Override
    public Long count(NodeOnOfflineLogPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .single();
    }

    @Override
    public void delete(NodeOnOfflineLogPaginationReq request) {
        new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    private WhereSql buildWhereSql(NodeOnOfflineLogPaginationReq request){
        return new WhereSql()
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andOnNotEmpty("group = ?", request.getGroup())
                .andOnNotEmpty("event = ?", request.getEvent())
                .andBetween("log_time", request.getStartLogTime(), request.getEndLogTime());
    }
}
