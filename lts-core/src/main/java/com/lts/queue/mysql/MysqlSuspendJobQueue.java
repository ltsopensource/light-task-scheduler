package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.core.support.JobQueueUtils;
import com.lts.queue.SuspendJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.mysql.support.RshHolder;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.admin.request.JobQueueReq;

/**
 * @author bug (357693306@qq.com) on 3/4/16.
 */
public class MysqlSuspendJobQueue extends AbstractMysqlJobQueue implements SuspendJobQueue {

    public MysqlSuspendJobQueue(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_cron_suspend_job_queue.sql", getTableName()));
    }

    @Override
    protected String getTableName(JobQueueReq request) {
        return getTableName();
    }

    @Override
    public boolean add(JobPo jobPo) {
        return add(getTableName(), jobPo);
    }

    @Override
    public JobPo getJob(String jobId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .single(RshHolder.JOB_PO_RSH);
    }

    @Override
    public boolean remove(String jobId) {
        return new DeleteSql(getSqlTemplate())
                .delete(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
    }

    private String getTableName() {
        return JobQueueUtils.SUSPEND_JOB_QUEUE;
    }

}
