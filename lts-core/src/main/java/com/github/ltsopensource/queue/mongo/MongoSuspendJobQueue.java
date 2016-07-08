package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.support.JobQueueUtils;
import com.github.ltsopensource.queue.SuspendJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author bug (357693306@qq.com) on 3/4/16.
 */
public class MongoSuspendJobQueue extends AbstractMongoJobQueue implements SuspendJobQueue {

    public MongoSuspendJobQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.SUSPEND_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_jobType", "jobType");
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex("idx_realTaskId_taskTrackerNodeGroup", "realTaskId, taskTrackerNodeGroup");
        }
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        return JobQueueUtils.SUSPEND_JOB_QUEUE;
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
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("taskId").equal(taskId).
                field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.get();
    }

}
