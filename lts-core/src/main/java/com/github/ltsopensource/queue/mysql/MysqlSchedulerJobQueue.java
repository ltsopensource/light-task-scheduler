package com.github.ltsopensource.queue.mysql;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.SchedulerJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.queue.mysql.support.RshHolder;
import com.github.ltsopensource.store.jdbc.builder.SelectSql;
import com.github.ltsopensource.store.jdbc.builder.UpdateSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 4/4/16.
 */
public abstract class MysqlSchedulerJobQueue extends AbstractMysqlJobQueue implements SchedulerJobQueue {

    public MysqlSchedulerJobQueue(Config config) {
        super(config);
    }

    @Override
    public boolean updateLastGenerateTriggerTime(String jobId, Long lastGenerateTriggerTime) {
        return new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName())
                .set("last_generate_trigger_time", lastGenerateTriggerTime)
                .set("gmt_modified", SystemClock.now())
                .where("job_id = ? ", jobId)
                .doUpdate() == 1;
    }

    @Override
    public List<JobPo> getNeedGenerateJobPos(Long checkTime, int topSize) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("rely_on_prev_cycle = ?", false)
                .and("last_generate_trigger_time <= ?", checkTime)
                .limit(0, topSize)
                .list(RshHolder.JOB_PO_LIST_RSH);
    }

    protected abstract String getTableName();
}
