package com.lts.admin.access.mysql;

import com.lts.admin.access.face.BackendJVMMemoryAccess;
import com.lts.admin.request.JvmDataReq;
import com.lts.core.cluster.Config;
import com.lts.monitor.access.mysql.MysqlJVMMemoryAccess;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.WhereSql;

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
                .delete(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    public WhereSql buildWhereSql(JvmDataReq req) {
        return new WhereSql()
                .andOnNotEmpty("identity = ?", req.getIdentity())
                .andBetween("timestamp", req.getStartTime(), req.getEndTime());

    }
}
