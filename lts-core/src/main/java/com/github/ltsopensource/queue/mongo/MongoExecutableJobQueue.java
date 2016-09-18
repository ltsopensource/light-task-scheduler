package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.JobQueueUtils;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.ExecutableJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;
import com.github.ltsopensource.store.jdbc.exception.JdbcException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoExecutableJobQueue extends AbstractMongoJobQueue implements ExecutableJobQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoExecutableJobQueue.class);

    public MongoExecutableJobQueue(Config config) {
        super(config);
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        if (StringUtils.isEmpty(taskTrackerNodeGroup)) {
            throw new JdbcException("taskTrackerNodeGroup can not be null");
        }
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
    }

    private ConcurrentHashSet<String> EXIST_TABLE = new ConcurrentHashSet<String>();

    @Override
    public boolean createQueue(String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex(tableName, "idx_jobId", "jobId", true, true);
            template.ensureIndex(tableName, "idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex(tableName, "idx_taskTrackerIdentity", "taskTrackerIdentity");
            template.ensureIndex(tableName, "idx_jobType", "jobType");
            template.ensureIndex(tableName, "idx_realTaskId_taskTrackerNodeGroup", "realTaskId, taskTrackerNodeGroup");
            template.ensureIndex(tableName, "idx_priority_triggerTime_gmtCreated", "priority,triggerTime,gmtCreated");
            template.ensureIndex(tableName, "idx_isRunning", "isRunning");
            LOGGER.info("create queue " + tableName);
        }
        EXIST_TABLE.add(tableName);
        return true;
    }

    @Override
    public boolean removeQueue(String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        dbCollection.drop();
        LOGGER.info("drop queue " + tableName);

        return true;
    }

    @Override
    public boolean add(JobPo jobPo) {
        try {
            String tableName = JobQueueUtils.getExecutableQueueName(jobPo.getTaskTrackerNodeGroup());
            if (!EXIST_TABLE.contains(tableName)) {
                createQueue(jobPo.getTaskTrackerNodeGroup());
            }
            jobPo.setGmtModified(jobPo.getGmtCreated());
            template.save(tableName, jobPo);
        } catch (DuplicateKeyException e) {
            // 已经存在
            throw new DupEntryException(e);
        }
        return true;
    }

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    @Override
    public long countJob(String realTaskId, String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("realTaskId").equal(realTaskId);
        query.field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.countAll();
    }

    @Override
    public boolean removeBatch(String realTaskId, String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("realTaskId").equal(realTaskId);
        query.field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        template.delete(query);
        return true;
    }

    public void resume(JobPo jobPo) {
        String tableName = JobQueueUtils.getExecutableQueueName(jobPo.getTaskTrackerNodeGroup());
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);

        query.field("jobId").equal(jobPo.getJobId());

        UpdateOperations<JobPo> operations =
                template.createUpdateOperations(JobPo.class)
                        .set("isRunning", false)
                        .set("taskTrackerIdentity", "")
                        .set("gmtModified", SystemClock.now());
        template.update(query, operations);
    }

    @Override
    public List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("isRunning").equal(true).
                filter("gmtCreated < ", deadline);
        return query.asList();
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
