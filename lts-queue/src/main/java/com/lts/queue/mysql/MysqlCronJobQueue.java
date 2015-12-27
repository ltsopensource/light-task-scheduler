package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.constant.Constants;
import com.lts.web.request.JobQueueRequest;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.CronJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.JobQueueException;
import com.lts.queue.mysql.support.ResultSetHandlerHolder;

import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlCronJobQueue extends AbstractMysqlJobQueue implements CronJobQueue {

    private String finishSQL = "SELECT * FROM `{tableName}` WHERE job_id = ?"
            .replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);

    private String selectSQL = "SELECT * FROM `{tableName}` WHERE task_id = ? AND task_tracker_node_group = ?"
            .replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);

    private String removeSQL = "DELETE FROM `{tableName}` WHERE job_id = ?"
            .replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);

    public MysqlCronJobQueue(Config config) {
        super(config);
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_cron_job_queue.sql");
            String sql = FileUtils.read(is, Constants.CHARSET);
            sql = sql.replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);
            getSqlTemplate().update(sql);
        } catch (Exception e) {
            throw new JobQueueException("create table error!", e);
        }
    }

    @Override
    protected String getTableName(JobQueueRequest request) {
        return JobQueueUtils.CRON_JOB_QUEUE;
    }

    @Override
    public boolean add(JobPo jobPo) {
        jobPo.setGmtCreated(SystemClock.now());
        jobPo.setGmtModified(jobPo.getGmtCreated());
        return super.add(JobQueueUtils.CRON_JOB_QUEUE, jobPo);
    }

    @Override
    public JobPo finish(String jobId) {
        try {
            return getSqlTemplate().query(finishSQL, ResultSetHandlerHolder.JOB_PO_RESULT_SET_HANDLER, jobId);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
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
    public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
        try {
            return getSqlTemplate().query(selectSQL, ResultSetHandlerHolder.JOB_PO_RESULT_SET_HANDLER, taskId, taskTrackerNodeGroup);
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

}
