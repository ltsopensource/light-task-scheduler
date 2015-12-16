package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.constant.Constants;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.ExecutableJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.JobQueueException;
import com.lts.queue.mysql.support.ResultSetHandlerHolder;
import com.lts.web.request.JobQueueRequest;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutableJobQueue extends AbstractMysqlJobQueue implements ExecutableJobQueue {

    // 用来缓存SQL，不用每次去生成，可以重用
    private final ConcurrentHashMap<String, String> SQL_CACHE_MAP = new ConcurrentHashMap<String, String>();

    public MysqlExecutableJobQueue(Config config) {
        super(config);
    }

    @Override
    protected String getTableName(JobQueueRequest request) {
        if (StringUtils.isEmpty(request.getTaskTrackerNodeGroup())) {
            throw new IllegalArgumentException(" takeTrackerNodeGroup cat not be null");
        }
        return getTableName(request.getTaskTrackerNodeGroup());
    }

    @Override
    public boolean createQueue(String taskTrackerNodeGroup) {
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_executable_job_queue.sql");
            String sql = FileUtils.read(is, Constants.CHARSET);
            getSqlTemplate().update(getRealSql(sql, taskTrackerNodeGroup));
            return true;
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
    }

    private String delTable = "DROP TABLE IF EXISTS {tableName};";

    @Override
    public boolean removeQueue(String taskTrackerNodeGroup) {
        try {
            getSqlTemplate().update(delTable.replace("{tableName}", JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup)));
            return true;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String getTableName(String taskTrackerNodeGroup) {
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
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

    @Override
    public boolean add(JobPo jobPo) {
        jobPo.setGmtCreated(SystemClock.now());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        try {
            return super.add(getTableName(jobPo.getTaskTrackerNodeGroup()), jobPo);
        } catch (JobQueueException e) {
            if (e.getMessage().contains("doesn't exist Query:")) {
                createQueue(jobPo.getTaskTrackerNodeGroup());
                add(jobPo);
            } else{
                throw e;
            }
        }
        return true;
    }

    private String removeSQL = "DELETE FROM `{tableName}` WHERE job_id = ?";

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        try {
            return getSqlTemplate().update(getRealSql(removeSQL, taskTrackerNodeGroup), jobId) == 1;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String resumeSQL = "UPDATE `{tableName}` SET " +
            "`is_running` = ?," +
            "`task_tracker_identity` = ?," +
            "`gmt_modified` = ?" +
            " WHERE job_id = ? ";

    @Override
    public void resume(JobPo jobPo) {
        try {
            Object[] params = new Object[]{false, null, SystemClock.now(), jobPo.getJobId()};
            getSqlTemplate().update(getRealSql(resumeSQL, jobPo.getTaskTrackerNodeGroup()), params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String getDeadJobSQL = "SELECT * FROM `{tableName}` WHERE is_running = ? AND gmt_modified < ?";

    @Override
    public List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
        try {
            return getSqlTemplate().query(getRealSql(getDeadJobSQL, taskTrackerNodeGroup), ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER, true, deadline);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    private String selectSQL = "SELECT * FROM `{tableName}` WHERE task_id = ? AND task_tracker_node_group = ?";

    @Override
    public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
        try {
            return getSqlTemplate().query(getRealSql(selectSQL, taskTrackerNodeGroup), ResultSetHandlerHolder.JOB_PO_RESULT_SET_HANDLER, taskId, taskTrackerNodeGroup);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }
}
