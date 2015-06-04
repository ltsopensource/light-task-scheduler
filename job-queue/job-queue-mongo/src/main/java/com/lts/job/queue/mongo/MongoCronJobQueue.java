package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.DateUtils;
import com.lts.job.core.util.JobQueueUtils;
import com.lts.job.queue.CronJobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.DuplicateJobException;
import com.lts.job.store.mongo.MongoRepository;
import com.mongodb.*;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoCronJobQueue extends MongoRepository implements CronJobQueue {

    public MongoCronJobQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.CRON_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.isEmpty(indexInfo)) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_taskTrackerNodeGroup_taskId", "taskTrackerNodeGroup,taskId", true, true);
        }
    }

    @Override
    public boolean add(JobPo jobPo) {
        try {
            jobPo.setGmtCreated(DateUtils.currentTimeMillis());
            jobPo.setGmtModified(jobPo.getGmtCreated());
            template.save(jobPo);
        } catch (DuplicateKeyException e) {
            // 已经存在
            throw new DuplicateJobException(e);
        }
        return true;
    }

    @Override
    public JobPo finish(String jobId) {
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
}
