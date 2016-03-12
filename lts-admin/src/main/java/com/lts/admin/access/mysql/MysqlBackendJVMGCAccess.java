package com.lts.admin.access.mysql;

import com.lts.admin.access.face.BackendJVMGCAccess;
import com.lts.admin.request.JvmDataReq;
import com.lts.core.cluster.Config;
import com.lts.monitor.access.mysql.MysqlJVMGCAccess;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.WhereSql;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MysqlBackendJVMGCAccess extends MysqlJVMGCAccess implements BackendJVMGCAccess {

    public MysqlBackendJVMGCAccess(Config config) {
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

    public WhereSql buildWhereSql(JvmDataReq req) {
        return new WhereSql()
                .andOnNotEmpty("identity = ?", req.getIdentity())
                .andBetween("timestamp", req.getStartTime(), req.getEndTime());

    }
}
