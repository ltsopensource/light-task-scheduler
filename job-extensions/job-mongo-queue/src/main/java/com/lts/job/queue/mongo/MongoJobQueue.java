package com.lts.job.queue.mongo;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.lts.job.queue.mongo.store.AbstractMongoRepository;
import com.lts.job.queue.mongo.store.Config;
import com.lts.job.tracker.queue.DuplicateJobException;
import com.lts.job.tracker.queue.JobPo;
import com.lts.job.tracker.queue.JobQueue;
import com.mongodb.MongoException;

import java.util.List;

/**
 * mongo 实现
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 */
public class MongoJobQueue extends AbstractMongoRepository<JobPo> implements JobQueue {

    public MongoJobQueue(Config config) {
        super(config);
    }

    @Override
    public boolean add(JobPo jobPo) throws DuplicateJobException {
        try {
            super.save(jobPo);
        } catch (MongoException.DuplicateKey e) {
            // 已经存在
            throw new DuplicateJobException(e);
        }
        return true;
    }

    @Override
    public JobPo take(String taskTrackerGroup, String taskTrackerIdentity) {
        // 优先级升序,时间升序
        Query<JobPo> query = createQuery();
        query.field("taskTrackerNodeGroup").equal(taskTrackerGroup)
                .field("isRunning").equal(false)
                .filter("triggerTime < ", System.currentTimeMillis())
                .order(" priority, triggerTime");

        UpdateOperations<JobPo> operations =
                ds.createUpdateOperations(JobPo.class)
                        .set("isRunning", true)
                        .set("taskTrackerIdentity", taskTrackerIdentity)
                        .set("gmtModify", System.currentTimeMillis())
                        .set("prevExeTime", System.currentTimeMillis());

        return ds.findAndModify(query, operations, false);
    }

    @Override
    public void resume(JobPo jobPo) {
        Query<JobPo> query = createQuery().field("jobId").equal(jobPo.getJobId());

        UpdateOperations<JobPo> operations =
                ds.createUpdateOperations(JobPo.class)
                        .set("isRunning", false)
                        .set("taskTrackerIdentity", "")
                        .set("gmtModify", System.currentTimeMillis());

        ds.update(query, operations);
    }

    @Override
    public void remove(String jobId) {
        Query<JobPo> query = createQuery().field("jobId").equal(jobId);
        ds.delete(query);
    }

    @Override
    public List<JobPo> getByLimitExecTime(long limitExecTime) {
        Query<JobPo> query = createQuery();
        query.field("isRunning").equal(true)
                .filter("gmtModify < ", System.currentTimeMillis() - limitExecTime);
        return query.asList();
    }

    @Override
    public List<JobPo> getRunningJob(String taskTrackerIdentity) {
        Query<JobPo> query = createQuery();
        query.field("taskTrackerIdentity").equal(taskTrackerIdentity)
                .field("isRunning").equal(true);
        return query.asList();
    }

    @Override
    public void updateScheduleTriggerTime(String jobId, Long triggerTime) {

        Query<JobPo> query = createQuery().field("jobId").equal(jobId);

        UpdateOperations<JobPo> operations = null;
        operations = ds.createUpdateOperations(JobPo.class)
                .set("isRunning", false)
                .set("gmtModify", System.currentTimeMillis())
                .set("triggerTime", triggerTime);

        ds.update(query, operations);
    }
}
