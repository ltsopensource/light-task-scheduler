package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.commons.file.FileUtils;
import com.lts.job.core.commons.utils.DateUtils;
import com.lts.job.core.support.JobQueueUtils;
import com.lts.job.queue.CronJobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.JobQueueException;
import com.lts.job.queue.mysql.support.ResultSetHandlerHolder;

import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlCronJobQueue extends AbstractMysqlJobQueue implements CronJobQueue {

    private String finishSQL = "SELECT * FROM `{tableName}` WHERE job_id = ?"
            .replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);

    private String removeSQL = "DELETE FROM `{tableName}` WHERE job_id = ?"
            .replace("{tableName}", JobQueueUtils.CRON_JOB_QUEUE);

    public MysqlCronJobQueue(Config config) {
        super(config);
        // create table
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sql/lts_cron_job_queue.sql");
            String sql = FileUtils.read(is);
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
        jobPo.setGmtCreated(DateUtils.currentTimeMillis());
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
            return getSqlTemplate().update(removeSQL, jobId) == 0;
        } catch (SQLException e) {
            throw new JobQueueException(e);
        }
    }

}
