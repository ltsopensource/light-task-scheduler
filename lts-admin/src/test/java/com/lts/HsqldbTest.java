package com.lts;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.store.jdbc.DataSourceProviderFactory;
import com.lts.store.jdbc.SqlTemplate;
import com.lts.web.repository.TaskTrackerMonitorDataPo;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert HG (254963746@qq.com) on 8/21/15.
 */
public class HsqldbTest {

    SqlTemplate sqlTemplate;

    @Before
    public void init() {
        Config config = new Config();
        config.setParameter("jdbc.datasource.provider", "hsqldb");
        config.setParameter("jdbc.url", "jdbc:hsqldb:file:/Users/hugui/.lts/hsqldb/lts-admin");
        config.setParameter("jdbc.username", "sa");
        config.setParameter("jdbc.password", "");
        sqlTemplate = new SqlTemplate(
                DataSourceProviderFactory.create(config)
                        .getDataSource(config));
    }

    @Test
    public void createTable() throws SQLException {
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
        sqlTemplate.update(tableSQL);

        // HSQLDB 不能在创建表的时候创建索引... see http://www.hsqldb.org/doc/guide/ch09.html#create_table-section
        String timestampIndex = "CREATE INDEX t_timestamp ON taskTrackerMonitor (timestamp ASC)";
        String taskTrackerIdentityIndex = "CREATE INDEX t_taskTrackerIdentity ON taskTrackerMonitor (taskTrackerIdentity ASC)";
        String taskTrackerNodeGroupIndex = "CREATE INDEX t_taskTrackerNodeGroup ON taskTrackerMonitor (taskTrackerNodeGroup ASC)";
        createIndex(timestampIndex);
        createIndex(taskTrackerIdentityIndex);
        createIndex(taskTrackerNodeGroupIndex);
    }

    private void createIndex(String indexSQL){
        try {
            sqlTemplate.update(indexSQL);
        } catch (SQLException ignore) {
            // ignore
        }
    }

    @Test
    public void insert() throws SQLException {
        String insertSQL = "INSERT INTO taskTrackerMonitor3 (" +
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
        TaskTrackerMonitorDataPo po = new TaskTrackerMonitorDataPo();
        po.setId("231321");
        po.setFreeMemory(321312L);

    }

    @Test
    public void select() throws SQLException {
        String selectSQL = "select distinct taskTrackerIdentity,taskTrackerNodeGroup  from taskTrackerMonitor";

        List<TaskTrackerMonitorDataPo> nodes = sqlTemplate.query(selectSQL, NODE_LIST_RESULT_SET_HANDLER, null);
        for (TaskTrackerMonitorDataPo node : nodes) {
            System.out.println(JSONUtils.toJSONString(node));
        }
    }

    private ResultSetHandler<List<TaskTrackerMonitorDataPo>> NODE_LIST_RESULT_SET_HANDLER = new ResultSetHandler<List<TaskTrackerMonitorDataPo>>() {
        @Override
        public List<TaskTrackerMonitorDataPo> handle(ResultSet rs) throws SQLException {
            List<TaskTrackerMonitorDataPo> pos = new ArrayList<TaskTrackerMonitorDataPo>();

            while (rs.next()) {
                TaskTrackerMonitorDataPo po = new TaskTrackerMonitorDataPo();
//                po.setId(rs.getString("id"));
//                po.setGmtCreated(rs.getLong("gmtCreated"));
                po.setTaskTrackerNodeGroup(rs.getString("taskTrackerNodeGroup"));
                po.setTaskTrackerIdentity(rs.getString("taskTrackerIdentity"));
//                po.setSuccessNum(rs.getLong("successNum"));
//                po.setFailedNum(rs.getLong("failedNum"));
//                po.setTotalRunningTime(rs.getLong("totalRunningTime"));
//                po.setFailStoreSize(rs.getLong("failStoreSize"));
//                po.setTimestamp(rs.getLong("timestamp"));
//                po.setMaxMemory(rs.getLong("maxMemory"));
//                po.setAllocatedMemory(rs.getLong("allocatedMemory"));
//                po.setFreeMemory(rs.getLong("freeMemory"));
//                po.setTotalFreeMemory(rs.getLong("totalFreeMemory"));
                pos.add(po);
            }
            return pos;
        }
    };
}
