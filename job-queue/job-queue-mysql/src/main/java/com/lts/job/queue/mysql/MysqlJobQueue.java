package com.lts.job.queue.mysql;

import com.alibaba.fastjson.TypeReference;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.queue.JobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.DuplicateJobException;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.store.jdbc.JdbcRepository;
import com.lts.job.store.jdbc.SqlExecutor;
import org.apache.commons.dbutils.ResultSetHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class MysqlJobQueue extends JdbcRepository implements JobQueue {

    ResultSetHandler<JobPo> jobPoResultSetHandler = new ResultSetHandler<JobPo>() {
        @Override
        public JobPo handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            return getJobPo(rs);
        }
    };

    private JobPo getJobPo(ResultSet rs) throws SQLException {
        JobPo jobPo = new JobPo();
        jobPo.setJobId(rs.getString("job_id"));
        jobPo.setPriority(rs.getInt("priority"));
        jobPo.setTaskId(rs.getString("task_id"));
        jobPo.setGmtCreated(rs.getLong("gmt_created"));
        jobPo.setGmtModified(rs.getLong("gmt_modified"));
        jobPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
        jobPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
        jobPo.setExtParams(JSONUtils.parse(rs.getString("ext_params"), new TypeReference<HashMap<String, String>>() {
        }));
        jobPo.setIsRunning(rs.getBoolean("is_running"));
        jobPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
        jobPo.setCronExpression(rs.getString("cron_expression"));
        jobPo.setNeedFeedback(rs.getBoolean("need_feedback"));
        jobPo.setPrevExeTime(rs.getLong("prev_exe_time"));
        jobPo.setTriggerTime(rs.getLong("trigger_time"));
        return jobPo;
    }

    ResultSetHandler<List<JobPo>> jobPoListResultSetHandler = new ResultSetHandler<List<JobPo>>() {
        @Override
        public List<JobPo> handle(ResultSet rs) throws SQLException {
            List<JobPo> jobPos = new ArrayList<JobPo>();
            while (rs.next()) {
                jobPos.add(getJobPo(rs));
            }
            return jobPos;
        }
    };

    public MysqlJobQueue(Config config) {
        super(config);
        doCreateTable();
    }

    private void doCreateTable() {
        // 创建表
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_job_po.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder createTableSql = new StringBuilder();
            String data = null;
            while ((data = br.readLine()) != null) {
                createTableSql.append(data);
            }
            getSqlTemplate().update(createTableSql.toString());
        } catch (SQLException e) {
            throw new RuntimeException("create table error!", e);
        } catch (IOException e) {
            throw new RuntimeException("create table error!", e);
        }
    }

    @Override
    public boolean add(JobPo jobPo) throws DuplicateJobException {
        String sql = "INSERT INTO " +
                "`lts_job_po` ( " +
                "`job_id`, " +
                "`priority`, " +
                "`task_id`, " +
                "`gmt_created`, " +
                "`gmt_modified`, " +
                "`submit_node_group`, " +
                "`task_tracker_node_group`, " +
                "`ext_params`, " +
                "`is_running`, " +
                "`task_tracker_identity`, " +
                "`need_feedback`, " +
                "`cron_expression`, " +
                "`prev_exe_time`, " +
                "`trigger_time`)" +
                "VALUES " +
                " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            getSqlTemplate().update(sql,
                    jobPo.getJobId(),
                    jobPo.getPriority(),
                    jobPo.getTaskId(),
                    jobPo.getGmtCreated(),
                    jobPo.getGmtModified(),
                    jobPo.getSubmitNodeGroup(),
                    jobPo.getTaskTrackerNodeGroup(),
                    JSONUtils.toJSONString(jobPo.getExtParams()),
                    jobPo.isRunning(),
                    jobPo.getTaskTrackerIdentity(),
                    jobPo.isNeedFeedback(),
                    jobPo.getCronExpression(),
                    jobPo.getPrevExeTime(),
                    jobPo.getTriggerTime());
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DuplicateJobException(e.getMessage(), e);
            } else {
                throw new JobQueueException(e.getMessage(), e);
            }
        }
    }

    @Override
    public JobPo take(final String taskTrackerGroup, final String taskTrackerIdentity) {

        return getSqlTemplate().executeInTransaction(new SqlExecutor<JobPo>() {
            @Override
            public JobPo run(Connection conn) throws SQLException {
                Long now = System.currentTimeMillis();
                // select for update
                String selectForUpdateSql = "SELECT *" +
                        " FROM `lts_job_po` " +
                        " WHERE `task_tracker_node_group` = ? " +
                        " AND is_running = ? " +
                        " AND `trigger_time` < ? " +
                        " ORDER BY `trigger_time` ASC, `priority` ASC, `gmt_created` ASC " +
                        " LIMIT 0, 1 FOR UPDATE";
                Object[] selectParams = new Object[]{taskTrackerGroup, false, now};
                JobPo jobPo = getSqlTemplate().query(conn, selectForUpdateSql,
                        jobPoResultSetHandler, selectParams
                );
                if (jobPo != null) {
                    String updateSql = "UPDATE `lts_job_po` SET " +
                            "`is_running` = ?, " +
                            "`task_tracker_identity` = ?, " +
                            "`gmt_modified` = ?," +
                            "`prev_exe_time` = ? " +
                            " WHERE job_id = ?";
                    Object[] params = new Object[]{
                            true, taskTrackerIdentity, now, now, jobPo.getJobId()
                    };
                    getSqlTemplate().update(conn, updateSql, params);

                    jobPo.setIsRunning(true);
                    jobPo.setTaskTrackerIdentity(taskTrackerIdentity);
                    jobPo.setGmtModified(now);
                    jobPo.setPrevExeTime(now);
                }
                return jobPo;
            }
        });
    }

    @Override
    public void resume(JobPo jobPo) {
        String updateSql = "UPDATE `lts_job_po` SET " +
                "`is_running` = ?," +
                "`task_tracker_identity` = ?," +
                "`gmt_modified` = ?" +
                " WHERE job_id = ? ";
        try {
            Object[] params = new Object[]{false, null, System.currentTimeMillis(), jobPo.getJobId()};
            getSqlTemplate().update(updateSql, params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public void remove(String jobId) {
        String deleteSql = "DELETE FROM `lts_job_po` WHERE job_id = ?";
        try {
            getSqlTemplate().update(deleteSql, jobId);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getByLimitExecTime(long limitExecTime) {
        String selectSql = "SELECT * FROM `lts_job_po` WHERE is_running = ? and prev_exe_time < ?";
        try {
            Object[] params = new Object[]{true, System.currentTimeMillis() - limitExecTime};
            return getSqlTemplate().query(selectSql, jobPoListResultSetHandler, params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getRunningJob(String taskTrackerIdentity) {

        String selectSql = "SELECT * FROM `lts_job_po` WHERE task_tracker_identity = ? and is_running = ?";
        try {
            Object[] params = new Object[]{taskTrackerIdentity, true};
            return getSqlTemplate().query(selectSql, jobPoListResultSetHandler, params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public void updateScheduleTriggerTime(String jobId, Long triggerTime) {
        String deleteSql = "UPDATE `lts_job_po` SET is_running = ?, gmt_modified = ? , trigger_time = ? WHERE job_id = ?";
        try {
            Object[] params = new Object[]{false, System.currentTimeMillis(), triggerTime, jobId};
            getSqlTemplate().update(deleteSql, params);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }
}
