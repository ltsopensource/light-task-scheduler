package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.support.JobQueueUtils;
import com.github.ltsopensource.queue.CronJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoCronJobQueue extends MongoSchedulerJobQueue implements CronJobQueue {

    public MongoCronJobQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.CRON_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex("idx_realTaskId_taskTrackerNodeGroup", "realTaskId, taskTrackerNodeGroup");
            template.ensureIndex("idx_relyOnPrevCycle_lgtt", "relyOnPrevCycle, lastGenerateTriggerTime");
        }
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        return JobQueueUtils.CRON_JOB_QUEUE;
    }

    @Override
    public boolean add(JobPo jobPo) {
        try {
            template.save(jobPo);
        } catch (DuplicateKeyException e) {
            // 已经存在
            throw new DupEntryException(e);
        }
        return true;
    }

    @Override
    public JobPo getJob(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        return query.get();
    }

    @Override
    public boolean remove(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    @Override
    public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("taskId").equal(taskId).
                field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.get();
    }

}
