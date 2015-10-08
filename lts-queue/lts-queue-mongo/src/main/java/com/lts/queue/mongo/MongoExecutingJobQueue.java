package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.support.JobQueueUtils;
import com.lts.core.support.SystemClock;
import com.lts.queue.ExecutingJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoExecutingJobQueue extends AbstractMongoJobQueue implements ExecutingJobQueue {

    public MongoExecutingJobQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.EXECUTING_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex("idx_taskTrackerIdentity", "taskTrackerIdentity");
            template.ensureIndex("idx_gmtCreated", "gmtCreated");
        }
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        return JobQueueUtils.EXECUTING_JOB_QUEUE;
    }

    @Override
    public boolean add(JobPo jobPo) {
        try {
            jobPo.setGmtCreated(SystemClock.now());
            jobPo.setGmtModified(jobPo.getGmtCreated());
            template.save(jobPo);
        } catch (DuplicateKeyException e) {
            // already exist
            throw new DuplicateJobException(e);
        }
        return true;
    }

    @Override
    public boolean remove(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    @Override
    public JobPo get(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        return query.get();
    }

    @Override
    public List<JobPo> getJobs(String taskTrackerIdentity) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("taskTrackerIdentity").equal(taskTrackerIdentity);
        return query.asList();
    }

    @Override
    public List<JobPo> getDeadJobs(long deadline) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.filter("gmtCreated < ", deadline);
        return query.asList();
    }

}
