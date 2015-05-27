package com.lts.job.queue.mongo;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.queue.JobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.queue.exception.DuplicateJobException;
import com.lts.job.store.mongo.AbstractMongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import java.util.List;

/**
 * mongo 实现
 *
 * @author Robert HG (254963746@qq.com) on 8/8/14.
 */
public class MongoJobQueue extends AbstractMongoRepository<JobPo> implements JobQueue {

    public MongoJobQueue(Config config) {
        super(config);
        doCreateTable();
    }

    protected void doCreateTable() {
        // 1. 待执行的任务队列
        // 创建 JobPo 索引
        Class<?> clazz = JobPo.class;
        DBCollection dbCollection = ds.getCollection(clazz);
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        if (CollectionUtils.isEmpty(indexInfo)) {
            ds.ensureIndex(clazz, "idx_jobId", "jobId", true, true);
            ds.ensureIndex(clazz, "idx_taskTrackerNodeGroup_taskId", "taskTrackerNodeGroup,taskId", true, true);
            ds.ensureIndex(clazz, "idx_taskTrackerIdentity", "taskTrackerIdentity", false, false);
            ds.ensureIndex(clazz, "idx_triggerTime_priority_gmtCreated", "triggerTime,priority,gmtCreated", false, false);
            ds.ensureIndex(clazz, "idx_isRunning", "isRunning", false, false);
            ds.ensureIndex(clazz, "idx_taskTrackerNodeGroup_isRunning_triggerTime", "taskTrackerNodeGroup,isRunning,triggerTime", false, false);
        }

        // 2. 正在执行的任务队列

        // 3. cronExpression的队列

    }

    @Override
    public boolean add(JobPo jobPo) throws DuplicateJobException {
        try {
            ds.save(jobPo);
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
                .order(" triggerTime, priority , gmtCreated");

        UpdateOperations<JobPo> operations =
                ds.createUpdateOperations(JobPo.class)
                        .set("isRunning", true)
                        .set("taskTrackerIdentity", taskTrackerIdentity)
                        .set("gmtModified", System.currentTimeMillis())
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
                        .set("gmtModified", System.currentTimeMillis());

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
                .filter("prevExeTime < ", System.currentTimeMillis() - limitExecTime);
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
                .set("gmtModified", System.currentTimeMillis())
                .set("triggerTime", triggerTime);

        ds.update(query, operations);
    }

}
