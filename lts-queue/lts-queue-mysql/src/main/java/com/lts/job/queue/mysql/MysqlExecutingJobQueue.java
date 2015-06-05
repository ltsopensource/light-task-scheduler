package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.file.FileUtils;
import com.lts.job.core.util.DateUtils;
import com.lts.job.core.util.JobQueueUtils;
import com.lts.job.queue.ExecutingJobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.queue.mysql.support.ResultSetHandlerHolder;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutingJobQueue extends AbstractMysqlJobQueue implements ExecutingJobQueue {

    public MysqlExecutingJobQueue(Config config) {
        super(config);
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_executing_job_queue.sql");
            String sql = FileUtils.read(is);
            sql = sql.replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);
            getSqlTemplate().update(sql);
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
    }

    @Override
    public boolean add(JobPo jobPo) {
        jobPo.setGmtCreated(DateUtils.currentTimeMillis());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        return super.add(JobQueueUtils.EXECUTING_JOB_QUEUE, jobPo);
    }

    @Override
    public boolean remove(String jobId) {
        String deleteSql = "DELETE FROM `{tableName}` WHERE job_id = ?"
                .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);
        try {
            return getSqlTemplate().update(deleteSql, jobId) == 1;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getJobs(String taskTrackerIdentity) {
        String selectSql = "SELECT * FROM `{tableName}` WHERE task_tracker_identity = ?"
                .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);
        try {
            return getSqlTemplate().query(selectSql,
                    ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                    taskTrackerIdentity);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getDeadJobs(long deadline) {
        String selectSql = "SELECT * FROM `{tableName}` WHERE gmt_created < ?"
                .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);
        try {
            return getSqlTemplate().query(selectSql,
                    ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                    deadline);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }
}
