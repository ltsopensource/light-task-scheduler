package com.lts.queue.mysql;

import com.lts.core.Application;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.AbstractPreLoader;
import com.lts.queue.domain.JobPo;
import com.lts.queue.mysql.support.ResultSetHandlerHolder;
import com.lts.store.jdbc.datasource.DataSourceProviderFactory;
import com.lts.store.jdbc.SqlTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public class MysqlPreLoader extends AbstractPreLoader {

    private SqlTemplate sqlTemplate;

    public MysqlPreLoader(Application application) {
        super(application);

        sqlTemplate = new SqlTemplate(
                DataSourceProviderFactory.create(application.getConfig())
                        .getDataSource(application.getConfig()));
    }


    private String taskUpdateSQL = "UPDATE `{tableName}` SET " +
            "`is_running` = ?, " +
            "`task_tracker_identity` = ?, " +
            "`gmt_modified` = ?" +
            " WHERE job_id = ? AND is_running = ? AND trigger_time = ? AND gmt_modified = ? ";

    @Override
    protected boolean lockJob(String taskTrackerNodeGroup, String jobId,
                              String taskTrackerIdentity,
                              Long triggerTime,
                              Long gmtModified) {
        try {
            int affectedRow = sqlTemplate.update(getRealSql(taskUpdateSQL, taskTrackerNodeGroup), true,
                    taskTrackerIdentity, SystemClock.now(), jobId, false, triggerTime, gmtModified);
            return affectedRow == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    private String takeSelectSQL = "SELECT *" +
            " FROM `{tableName}` " +
            " WHERE is_running = ? " +
            " AND `trigger_time` < ? " +
            " ORDER BY `trigger_time` ASC, `priority` ASC, `gmt_created` ASC " +
            " LIMIT ?, ?";

    @Override
    protected List<JobPo> load(String loadTaskTrackerNodeGroup, int loadSize) {
        try {
            Long now = SystemClock.now();
            return sqlTemplate.query(getRealSql(takeSelectSQL, loadTaskTrackerNodeGroup),
                    ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                    false, now, 0, loadSize);
        } catch (SQLException e) {
            return null;
        }
    }

    private String getRealSql(String sql, String taskTrackerNodeGroup) {
        String key = sql.concat(taskTrackerNodeGroup);
        String fineSQL = SQL_CACHE_MAP.get(key);
        // 这里可以不用锁，多生成一次也不会产生什么问题
        if (fineSQL == null) {
            fineSQL = sql.replace("{tableName}", getTableName(taskTrackerNodeGroup));
            SQL_CACHE_MAP.put(key, fineSQL);
        }
        return fineSQL;
    }

    private final ConcurrentHashMap<String, String> SQL_CACHE_MAP = new ConcurrentHashMap<String, String>();

    private String getTableName(String taskTrackerNodeGroup) {
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
    }
}
