package com.lts.monitor.access.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.monitor.access.domain.JVMThreadDataPo;
import com.lts.monitor.access.face.JVMThreadAccess;
import com.lts.store.jdbc.builder.InsertSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlJVMThreadAccess extends MysqlAbstractJdbcAccess implements JVMThreadAccess {

    public MysqlJVMThreadAccess(Config config) {
        super(config);
    }

    @Override
    protected String getTableName() {
        return "lts_admin_jvm_thread";
    }

    @Override
    public void insert(List<JVMThreadDataPo> jvmThreadDataPos) {
        if (CollectionUtils.isEmpty(jvmThreadDataPos)) {
            return;
        }

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "identity",
                        "timestamp",
                        "node_type",
                        "node_group",
                        "daemon_thread_count",
                        "thread_count",
                        "total_started_thread_count",
                        "dead_locked_thread_count",
                        "process_cpu_time_rate");

        for (JVMThreadDataPo po : jvmThreadDataPos) {
            insertSql.values(
                    po.getGmtCreated(),
                    po.getIdentity(),
                    po.getTimestamp(),
                    po.getNodeType().name(),
                    po.getNodeGroup(),
                    po.getDaemonThreadCount(),
                    po.getThreadCount(),
                    po.getTotalStartedThreadCount(),
                    po.getDeadLockedThreadCount(),
                    po.getProcessCpuTimeRate()
            );
        }

        insertSql.doBatchInsert();
    }

}
