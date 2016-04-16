package com.github.ltsopensource.queue.mongo;

import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.SchedulerJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 4/4/16.
 */
public abstract class MongoSchedulerJobQueue extends AbstractMongoJobQueue implements SchedulerJobQueue {

    public MongoSchedulerJobQueue(Config config) {
        super(config);
    }

    @Override
    public boolean updateLastGenerateTriggerTime(String jobId, Long lastGenerateTriggerTime) {
        Query<JobPo> query = template.createQuery(getTableName(), JobPo.class);

        query.field("jobId").equal(jobId);

        UpdateOperations<JobPo> operations =
                template.createUpdateOperations(JobPo.class)
                        .set("lastGenerateTriggerTime", lastGenerateTriggerTime)
                        .set("gmtModified", SystemClock.now());

        UpdateResults ur = template.update(query, operations);
        return ur.getUpdatedCount() == 1;
    }

    @Override
    public List<JobPo> getNeedGenerateJobPos(Long checkTime, int topSize) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("relyOnPrevCycle").equal(false);
        query.field("lastGenerateTriggerTime").equal(checkTime);
        query.offset(0).limit(topSize);
        return query.asList();
    }
}
