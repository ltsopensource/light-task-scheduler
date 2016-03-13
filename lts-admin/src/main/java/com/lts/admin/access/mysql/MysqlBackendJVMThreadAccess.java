package com.lts.admin.access.mysql;

import com.lts.admin.access.RshHandler;
import com.lts.admin.access.face.BackendJVMThreadAccess;
import com.lts.admin.request.JvmDataReq;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.core.cluster.Config;
import com.lts.monitor.access.domain.JVMThreadDataPo;
import com.lts.monitor.access.mysql.MysqlJVMThreadAccess;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.store.jdbc.builder.WhereSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlBackendJVMThreadAccess extends MysqlJVMThreadAccess implements BackendJVMThreadAccess {

    public MysqlBackendJVMThreadAccess(Config config) {
        super(config);
    }

    @Override
    public void delete(JvmDataReq request) {
        new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    @Override
    public List<JVMThreadDataPo> queryAvg(MDataPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("timestamp",
                        "AVG(daemon_thread_count) AS daemon_thread_count",
                        "AVG(thread_count) AS thread_count",
                        "AVG(total_started_thread_count) AS total_started_thread_count",
                        "AVG(dead_locked_thread_count) AS dead_locked_thread_count",
                        "AVG(process_cpu_time_rate) AS process_cpu_time_rate")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .groupBy(" timestamp ASC ")
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.JVM_THREAD_SUM_M_DATA_RSH);
    }

    public WhereSql buildWhereSql(JvmDataReq req) {
        return new WhereSql()
                .andOnNotEmpty("identity = ?", req.getIdentity())
                .andBetween("timestamp", req.getStartTime(), req.getEndTime());

    }

    public WhereSql buildWhereSql(MDataPaginationReq request) {
        return new WhereSql()
                .andOnNotNull("id = ?", request.getId())
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andOnNotEmpty("node_group = ?", request.getNodeGroup())
                .andBetween("timestamp", request.getStartTime(), request.getEndTime());
    }
}
