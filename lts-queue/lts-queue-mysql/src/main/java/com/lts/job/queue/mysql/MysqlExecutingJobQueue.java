package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.commons.file.FileUtils;
import com.lts.job.core.commons.utils.DateUtils;
import com.lts.job.core.support.JobQueueUtils;
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

    private String removeSQL = "DELETE FROM `{tableName}` WHERE job_id = ?"
            .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);

    private String selectSQL = "SELECT * FROM `{tableName}` WHERE task_tracker_identity = ?"
            .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);

    private String getDeadJobSQL = "SELECT * FROM `{tableName}` WHERE gmt_created < ?"
            .replace("{tableName}", JobQueueUtils.EXECUTING_JOB_QUEUE);

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
    protected String getTableName(JobQueueRequest request) {
        return JobQueueUtils.EXECUTING_JOB_QUEUE;
    }

    @Override
    public boolean add(JobPo jobPo) {
        jobPo.setGmtCreated(DateUtils.currentTimeMillis());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        return super.add(JobQueueUtils.EXECUTING_JOB_QUEUE, jobPo);
    }

    @Override
    public boolean remove(String jobId) {
        try {
            return getSqlTemplate().update(removeSQL, jobId) == 1;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getJobs(String taskTrackerIdentity) {
        try {
            return getSqlTemplate().query(selectSQL,
                    ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                    taskTrackerIdentity);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

    @Override
    public List<JobPo> getDeadJobs(long deadline) {
        try {
            return getSqlTemplate().query(getDeadJobSQL,
                    ResultSetHandlerHolder.JOB_PO_LIST_RESULT_SET_HANDLER,
                    deadline);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }
}
