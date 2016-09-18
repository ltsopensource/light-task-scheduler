package com.github.ltsopensource.admin.access.mysql;

import com.github.ltsopensource.admin.access.RshHandler;
import com.github.ltsopensource.admin.access.face.BackendJVMMemoryAccess;
import com.github.ltsopensource.admin.request.JvmDataReq;
import com.github.ltsopensource.admin.request.MDataPaginationReq;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.monitor.access.domain.JVMMemoryDataPo;
import com.github.ltsopensource.monitor.access.mysql.MysqlJVMMemoryAccess;
import com.github.ltsopensource.store.jdbc.builder.DeleteSql;
import com.github.ltsopensource.store.jdbc.builder.SelectSql;
import com.github.ltsopensource.store.jdbc.builder.WhereSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlBackendJVMMemoryAccess extends MysqlJVMMemoryAccess implements BackendJVMMemoryAccess {

    public MysqlBackendJVMMemoryAccess(Config config) {
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
    public List<JVMMemoryDataPo> queryAvg(MDataPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("timestamp",
                        "AVG(heap_memory_committed) AS heap_memory_committed",
                        "AVG(heap_memory_init) AS heap_memory_init",
                        "AVG(heap_memory_max) AS heap_memory_max",
                        "AVG(heap_memory_used) AS heap_memory_used",
                        "AVG(non_heap_memory_committed) AS non_heap_memory_committed",
                        "AVG(non_heap_memory_init) AS non_heap_memory_init",
                        "AVG(non_heap_memory_max) AS non_heap_memory_max",
                        "AVG(non_heap_memory_used) AS non_heap_memory_used",
                        "AVG(perm_gen_committed) AS perm_gen_committed",
                        "AVG(perm_gen_init) AS perm_gen_init",
                        "AVG(perm_gen_max) AS perm_gen_max",
                        "AVG(perm_gen_used) AS perm_gen_used",
                        "AVG(old_gen_committed) AS old_gen_committed",
                        "AVG(old_gen_init) AS old_gen_init",
                        "AVG(old_gen_max) AS old_gen_max",
                        "AVG(old_gen_used) AS old_gen_used",
                        "AVG(eden_space_committed) AS eden_space_committed",
                        "AVG(eden_space_init) AS eden_space_init",
                        "AVG(eden_space_max) AS eden_space_max",
                        "AVG(eden_space_used) AS eden_space_used",
                        "AVG(survivor_committed) AS survivor_committed",
                        "AVG(survivor_init) AS survivor_init",
                        "AVG(survivor_max) AS survivor_max",
                        "AVG(survivor_used) AS survivor_used")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .groupBy(" timestamp ASC ")
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.JVM_MEMORY_SUM_M_DATA_RSH);
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
