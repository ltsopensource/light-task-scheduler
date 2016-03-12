package com.lts.admin.access.mysql;

import com.lts.admin.access.RshHandler;
import com.lts.admin.access.face.BackendJobTrackerMAccess;
import com.lts.admin.request.MDataPaginationReq;
import com.lts.core.cluster.Config;
import com.lts.monitor.access.domain.JobTrackerMDataPo;
import com.lts.monitor.access.mysql.MysqlJobTrackerMAccess;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.store.jdbc.builder.WhereSql;
import com.lts.store.jdbc.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class MysqlBackendJobTrackerMAccess extends MysqlJobTrackerMAccess implements BackendJobTrackerMAccess {

    public MysqlBackendJobTrackerMAccess(Config config) {
        super(config);
    }

    @Override
    public List<JobTrackerMDataPo> querySum(MDataPaginationReq request) {

        return new SelectSql(getSqlTemplate())
                .select()
                .columns("timestamp",
                        "SUM(receive_job_num) AS receive_job_num",
                        "SUM(push_job_num) AS push_job_num" ,
                        "SUM(exe_success_num) AS exe_success_num" ,
                        "SUM(exe_failed_num) AS exe_failed_num" ,
                        "SUM(exe_later_num) AS exe_later_num" ,
                        "SUM(exe_exception_num) AS exe_exception_num" ,
                        "SUM(fix_executing_job_num) AS fix_executing_job_num")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .groupBy(" timestamp ASC ")
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.JOB_TRACKER_SUM_M_DATA_RSH);
    }

    private WhereSql buildWhereSql(MDataPaginationReq request) {
        return new WhereSql()
                .andOnNotEmpty("id = ? ", request.getId())
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andBetween("timestamp", request.getStartTime(), request.getEndTime());
    }

    @Override
    public void delete(MDataPaginationReq request) {

        new DeleteSql(getSqlTemplate())
                .delete(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    @Override
    public List<String> getJobTrackers() {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("DISTINCT identity AS `identity` ")
                .from()
                .table(getTableName())
                .list(new ResultSetHandler<List<String>>() {
                    @Override
                    public List<String> handle(ResultSet rs) throws SQLException {
                        List<String> list = new ArrayList<String>();
                        while (rs.next()) {
                            list.add(rs.getString("identity"));
                        }
                        return list;
                    }
                });
    }

}
