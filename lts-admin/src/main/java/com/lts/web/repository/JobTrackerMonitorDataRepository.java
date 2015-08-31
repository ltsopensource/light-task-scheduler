package com.lts.web.repository;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.exception.DaoException;
import com.lts.store.jdbc.SqlBuilder;
import com.lts.web.request.MonitorDataRequest;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Repository
public class JobTrackerMonitorDataRepository extends HsqlRepository {

    public JobTrackerMonitorDataRepository() {
        createTable();
    }

    /**
     * 创建表
     */
    private void createTable() {
        String tableSQL = "CREATE TABLE IF NOT EXISTS jobTrackerMonitor(" +
                " id varchar(64)," +
                " gmtCreated bigint," +
                " jobTrackerIdentity varchar(64)," +
                " receiveJobNum bigint," +
                " pushJobNum bigint," +
                " exeSuccessNum bigint," +
                " exeFailedNum bigint," +
                " exeLaterNum bigint," +
                " exeExceptionNum bigint," +
                " fixExecutingJobNum bigint," +
                " timestamp bigint," +
                " maxMemory bigint," +
                " allocatedMemory bigint," +
                " freeMemory bigint," +
                " totalFreeMemory bigint," +
                " PRIMARY KEY (id))";
        try {
            getSqlTemplate().update(tableSQL);

            // HSQLDB 不能在创建表的时候创建索引... see http://www.hsqldb.org/doc/guide/ch09.html#create_table-section
            String timestampIndex = "CREATE INDEX t_timestamp ON jobTrackerMonitor (timestamp ASC)";
            String jobTrackerIdentityIndex = "CREATE INDEX t_jobTrackerIdentity ON taskTrackerMonitor (jobTrackerIdentity ASC)";
            createIndex(timestampIndex);
            createIndex(jobTrackerIdentityIndex);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createIndex(String indexSQL) {
        try {
            getSqlTemplate().update(indexSQL);
        } catch (SQLException ignore) {
            // ignore
        }
    }

    private String insertSQL = "INSERT INTO jobTrackerMonitor (" +
            "id," +
            "gmtCreated," +
            "jobTrackerIdentity," +
            "receiveJobNum," +
            "pushJobNum," +
            "exeSuccessNum," +
            "exeFailedNum," +
            "exeLaterNum," +
            "exeExceptionNum," +
            "fixExecutingJobNum," +
            "timestamp," +
            "maxMemory," +
            "allocatedMemory," +
            "freeMemory," +
            "totalFreeMemory" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void insert(List<JobTrackerMonitorDataPo> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return;
        }
        for (JobTrackerMonitorDataPo po : pos) {
            try {
                getSqlTemplate().update(insertSQL,
                        po.getId(),
                        po.getGmtCreated(),
                        po.getJobTrackerIdentity(),
                        po.getReceiveJobNum(),
                        po.getPushJobNum(),
                        po.getExeSuccessNum(),
                        po.getExeFailedNum(),
                        po.getExeLaterNum(),
                        po.getExeExceptionNum(),
                        po.getFixExecutingJobNum(),
                        po.getTimestamp(),
                        po.getMaxMemory(),
                        po.getAllocatedMemory(),
                        po.getFreeMemory(),
                        po.getTotalFreeMemory()
                );
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("unique constraint")) {
                    // 主键重复 ignore
                } else {
                    throw new DaoException(e);
                }
            }
        }
    }

    public List<JobTrackerMonitorDataPo> querySum(MonitorDataRequest request) {

        String sql = "SELECT " +
                "timestamp," +
                "SUM(receiveJobNum) AS receiveJobNum, " +
                "SUM(pushJobNum) AS pushJobNum," +
                "SUM(exeSuccessNum) AS exeSuccessNum," +
                "SUM(exeFailedNum) AS exeFailedNum," +
                "SUM(exeLaterNum) AS exeLaterNum," +
                "SUM(exeExceptionNum) AS exeExceptionNum," +
                "SUM(fixExecutingJobNum) AS fixExecutingJobNum," +
                "SUM(maxMemory) AS maxMemory," +
                "SUM(allocatedMemory) AS allocatedMemory," +
                "SUM(freeMemory) AS freeMemory," +
                "SUM(totalFreeMemory) AS totalFreeMemory" +
                " FROM jobTrackerMonitor" +
                " WHERE ";
        StringBuilder whereSQL = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            whereSQL.append("AND jobTrackerIdentity=?");
            params.add(request.getIdentity());
        }
        whereSQL.append("AND timestamp >= ? AND timestamp <= ?");
        params.add(request.getStartTime());
        params.add(request.getEndTime());

        sql = sql + whereSQL.delete(0, 3) + " GROUP BY timestamp ORDER BY timestamp ASC limit ?,?";
        params.add(request.getStart());
        params.add(request.getLimit());
        try {
            return getSqlTemplate().query(sql, RESULT_SET_HANDLER, params.toArray());
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    public List<JobTrackerMonitorDataPo> search(MonitorDataRequest request) {
        try {
            SqlBuilder sql = new SqlBuilder("SELECT * FROM jobTrackerMonitor");
            sql.addCondition("id", request.getId())
                    .addCondition("jobTrackerIdentity", request.getIdentity())
                    .addCondition("timestamp", request.getStartTime(), ">=")
                    .addCondition("timestamp", request.getEndTime(), "<=")
                    .addOrderBy(request.getField(), request.getDirection())
                    .addLimit(request.getStart(), request.getLimit());

            return getSqlTemplate().query(sql.getSQL(), RESULT_SET_HANDLER, sql.getParams().toArray());
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    public void delete(MonitorDataRequest request) {
        SqlBuilder sql = new SqlBuilder("DELETE FROM jobTrackerMonitor ");
                sql.addCondition("id", request.getId())
                .addCondition("jobTrackerIdentity", request.getIdentity())
                .addCondition("timestamp", request.getStartTime(), ">=")
                .addCondition("timestamp", request.getEndTime(), "<=");
        try {
            getSqlTemplate().update(sql.getSQL(), sql.getParams().toArray());
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<String> getJobTrackers() {
        String selectSQL = "select distinct jobTrackerIdentity from jobTrackerMonitor";
        try {
            return getSqlTemplate().query(selectSQL, TT_RESULT_SET_HANDLER);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    private ResultSetHandler<List<JobTrackerMonitorDataPo>> RESULT_SET_HANDLER = new ResultSetHandler<List<JobTrackerMonitorDataPo>>() {
        @Override
        public List<JobTrackerMonitorDataPo> handle(ResultSet rs) throws SQLException {
            List<JobTrackerMonitorDataPo> pos = new ArrayList<JobTrackerMonitorDataPo>();

            while (rs.next()) {
                JobTrackerMonitorDataPo po = new JobTrackerMonitorDataPo();
                po.setReceiveJobNum(rs.getLong("receiveJobNum"));
                po.setPushJobNum(rs.getLong("pushJobNum"));
                po.setExeSuccessNum(rs.getLong("exeSuccessNum"));
                po.setExeFailedNum(rs.getLong("exeFailedNum"));
                po.setExeLaterNum(rs.getLong("exeLaterNum"));
                po.setExeExceptionNum(rs.getLong("exeExceptionNum"));
                po.setFixExecutingJobNum(rs.getLong("fixExecutingJobNum"));
                po.setTimestamp(rs.getLong("timestamp"));
                po.setMaxMemory(rs.getLong("maxMemory"));
                po.setAllocatedMemory(rs.getLong("allocatedMemory"));
                po.setFreeMemory(rs.getLong("freeMemory"));
                po.setTotalFreeMemory(rs.getLong("totalFreeMemory"));
                pos.add(po);
            }
            return pos;
        }
    };

    private ResultSetHandler<List<String>> TT_RESULT_SET_HANDLER = new ResultSetHandler<List<String>>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> identities = new ArrayList<String>();
            while (rs.next()) {
                String jobTrackerIdentity = rs.getString("jobTrackerIdentity");
                identities.add(jobTrackerIdentity);
            }
            return identities;
        }
    };
}
