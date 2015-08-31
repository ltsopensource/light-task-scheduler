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
public class TaskTrackerMonitorDataRepository extends HsqlRepository {

    public TaskTrackerMonitorDataRepository() {
        createTable();
    }

    /**
     * 创建表
     */
    private void createTable() {
        String tableSQL = "CREATE TABLE IF NOT EXISTS taskTrackerMonitor(" +
                " id varchar(64)," +
                " gmtCreated bigint," +
                " taskTrackerNodeGroup varchar(64)," +
                " taskTrackerIdentity varchar(64)," +
                " successNum bigint," +
                " failedNum bigint," +
                " totalRunningTime bigint," +
                " failStoreSize bigint," +
                " timestamp bigint," +
                " maxMemory bigint," +
                " allocatedMemory bigint," +
                " freeMemory bigint," +
                " totalFreeMemory bigint," +
                " PRIMARY KEY (id))";
        try {
            getSqlTemplate().update(tableSQL);

            // HSQLDB 不能在创建表的时候创建索引... see http://www.hsqldb.org/doc/guide/ch09.html#create_table-section
            String timestampIndex = "CREATE INDEX t_timestamp ON taskTrackerMonitor (timestamp ASC)";
            String taskTrackerIdentityIndex = "CREATE INDEX t_taskTrackerIdentity ON taskTrackerMonitor (taskTrackerIdentity ASC)";
            String taskTrackerNodeGroupIndex = "CREATE INDEX t_taskTrackerNodeGroup ON taskTrackerMonitor (taskTrackerNodeGroup ASC)";
            createIndex(timestampIndex);
            createIndex(taskTrackerIdentityIndex);
            createIndex(taskTrackerNodeGroupIndex);
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

    private String insertSQL = "INSERT INTO taskTrackerMonitor (" +
            "id," +
            "gmtCreated," +
            "taskTrackerNodeGroup," +
            "taskTrackerIdentity," +
            "successNum," +
            "failedNum," +
            "totalRunningTime," +
            "failStoreSize," +
            "timestamp," +
            "maxMemory," +
            "allocatedMemory," +
            "freeMemory," +
            "totalFreeMemory" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public void insert(List<TaskTrackerMonitorDataPo> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return;
        }
        for (TaskTrackerMonitorDataPo po : pos) {
            try {
                getSqlTemplate().update(insertSQL,
                        po.getId(),
                        po.getGmtCreated(),
                        po.getTaskTrackerNodeGroup(),
                        po.getTaskTrackerIdentity(),
                        po.getSuccessNum(),
                        po.getFailedNum(),
                        po.getTotalRunningTime(),
                        po.getFailStoreSize(),
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

    public List<TaskTrackerMonitorDataPo> querySum(MonitorDataRequest request) {

        String sql = "SELECT " +
                "timestamp," +
                "SUM(successNum) AS successNum, " +
                "SUM(failedNum) AS failedNum," +
                "SUM(totalRunningTime) AS totalRunningTime," +
                "SUM(failStoreSize) AS failStoreSize," +
                "SUM(maxMemory) AS maxMemory," +
                "SUM(allocatedMemory) AS allocatedMemory," +
                "SUM(freeMemory) AS freeMemory," +
                "SUM(totalFreeMemory) AS totalFreeMemory" +
                " FROM taskTrackerMonitor" +
                " WHERE ";
        StringBuilder whereSQL = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            whereSQL.append("AND taskTrackerIdentity=?");
            params.add(request.getIdentity());
        }
        if (StringUtils.isNotEmpty(request.getNodeGroup())) {
            whereSQL.append("AND taskTrackerNodeGroup=?");
            params.add(request.getNodeGroup());
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

    public List<TaskTrackerMonitorDataPo> search(MonitorDataRequest request) {
        try {
            SqlBuilder sql = new SqlBuilder("SELECT * FROM taskTrackerMonitor");
            sql.addCondition("id", request.getId())
                    .addCondition("taskTrackerIdentity", request.getIdentity())
                    .addCondition("taskTrackerNodeGroup", request.getNodeGroup())
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
        SqlBuilder sql = new SqlBuilder("DELETE FROM taskTrackerMonitor ");
                sql.addCondition("id", request.getId())
                .addCondition("taskTrackerIdentity", request.getIdentity())
                .addCondition("taskTrackerNodeGroup", request.getNodeGroup())
                .addCondition("timestamp", request.getStartTime(), ">=")
                .addCondition("timestamp", request.getEndTime(), "<=");
        try {
            getSqlTemplate().update(sql.getSQL(), sql.getParams().toArray());
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Map<String, List<String>> getTaskTrackerMap() {
        String selectSQL = "select distinct taskTrackerIdentity,taskTrackerNodeGroup  from taskTrackerMonitor";
        try {
            return getSqlTemplate().query(selectSQL, TT_RESULT_SET_HANDLER);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    private ResultSetHandler<List<TaskTrackerMonitorDataPo>> RESULT_SET_HANDLER = new ResultSetHandler<List<TaskTrackerMonitorDataPo>>() {
        @Override
        public List<TaskTrackerMonitorDataPo> handle(ResultSet rs) throws SQLException {
            List<TaskTrackerMonitorDataPo> pos = new ArrayList<TaskTrackerMonitorDataPo>();

            while (rs.next()) {
                TaskTrackerMonitorDataPo po = new TaskTrackerMonitorDataPo();
//                po.setId(rs.getString("id"));
//                po.setGmtCreated(rs.getLong("gmtCreated"));
//                po.setTaskTrackerNodeGroup(rs.getString("taskTrackerNodeGroup"));
//                po.setTaskTrackerIdentity(rs.getString("taskTrackerIdentity"));
                po.setSuccessNum(rs.getLong("successNum"));
                po.setFailedNum(rs.getLong("failedNum"));
                po.setTotalRunningTime(rs.getLong("totalRunningTime"));
                po.setFailStoreSize(rs.getLong("failStoreSize"));
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

    private ResultSetHandler<Map<String, List<String>>> TT_RESULT_SET_HANDLER = new ResultSetHandler<Map<String, List<String>>>() {
        @Override
        public Map<String, List<String>> handle(ResultSet rs) throws SQLException {
            Map<String, List<String>> map = new HashMap<String, List<String>>();

            while (rs.next()) {
                String taskTrackerNodeGroup = rs.getString("taskTrackerNodeGroup");
                String taskTrackerIdentity = rs.getString("taskTrackerIdentity");
                List<String> identityList = map.get(taskTrackerNodeGroup);
                if (identityList == null) {
                    identityList = new ArrayList<String>();
                    identityList.add(taskTrackerIdentity);
                    map.put(taskTrackerNodeGroup, identityList);
                } else {
                    identityList.add(taskTrackerIdentity);
                }
            }
            return map;
        }
    };
}
