package com.lts.queue.mysql;

import com.lts.admin.request.JobQueueReq;
import com.lts.core.cluster.Config;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.RepeatJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.mysql.support.RshHolder;
import com.lts.store.jdbc.builder.DeleteSql;
import com.lts.store.jdbc.builder.SelectSql;
import com.lts.store.jdbc.builder.UpdateSql;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class MysqlRepeatJobQueue extends MysqlSchedulerJobQueue implements RepeatJobQueue {

    public MysqlRepeatJobQueue(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_repeat_job_queue.sql", getTableName()));
    }

    @Override
    protected String getTableName(JobQueueReq request) {
        return getTableName();
    }

    @Override
    public boolean add(JobPo jobPo) {
        return super.add(getTableName(), jobPo);
    }

    @Override
    public JobPo getJob(String jobId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .single(RshHolder.JOB_PO_RSH);
    }

    @Override
    public boolean remove(String jobId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
    }

    @Override
    public JobPo getJob(String taskTrackerNodeGroup, String taskId) {

        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
    }

    @Override
    public int incRepeatedCount(String jobId) {
        while (true) {
            JobPo jobPo = getJob(jobId);
            if (jobPo == null) {
                return -1;
            }
            if (new UpdateSql(getSqlTemplate())
                    .update()
                    .table(getTableName())
                    .set("repeated_count", jobPo.getRepeatedCount() + 1)
                    .where("job_id = ?", jobId)
                    .and("repeated_count = ?", jobPo.getRepeatedCount())
                    .doUpdate() == 1) {
                return jobPo.getRepeatedCount() + 1;
            }
        }
    }

    protected String getTableName() {
        return JobQueueUtils.REPEAT_JOB_QUEUE;
    }

}
