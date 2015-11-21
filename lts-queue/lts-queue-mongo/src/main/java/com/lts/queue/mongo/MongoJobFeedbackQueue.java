package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.JobQueueUtils;
import com.lts.queue.JobFeedbackQueue;
import com.lts.queue.domain.JobFeedbackPo;
import com.lts.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * mongo 实现
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobFeedbackQueue extends MongoRepository implements JobFeedbackQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoJobFeedbackQueue.class);

    public MongoJobFeedbackQueue(Config config) {
        super(config);
    }

    @Override
    public boolean createQueue(String jobClientNodeGroup) {
        String tableName = JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex(tableName, "idx_gmtCreated", "gmtCreated");
            LOGGER.info("create queue " + tableName);
        }
        return true;
    }

    @Override
    public boolean removeQueue(String jobClientNodeGroup) {
        String tableName = JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        dbCollection.drop();
        LOGGER.info("drop queue " + tableName);
        return true;
    }

    @Override
    public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return true;
        }
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            String tableName = JobQueueUtils.getFeedbackQueueName(
                    jobFeedbackPo.getTaskTrackerJobResult().getJobWrapper().getJob().getSubmitNodeGroup());
            try {
                template.save(tableName, jobFeedbackPo);
            } catch (DuplicateKeyException e) {
                LOGGER.warn("duplicate key for job feedback po: " + JSON.toJSONString(jobFeedbackPo));
            }
        }
        return true;
    }

    @Override
    public boolean remove(String jobClientNodeGroup, String id) {
        Query<JobFeedbackPo> query = createQuery(jobClientNodeGroup);
        query.field("id").equal(id);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    private Query<JobFeedbackPo> createQuery(String jobClientNodeGroup) {
        String tableName = JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup);
        return template.createQuery(tableName, JobFeedbackPo.class);
    }

    @Override
    public long getCount(String jobClientNodeGroup) {
        Query<JobFeedbackPo> query = createQuery(jobClientNodeGroup);
        return template.getCount(query);
    }

    @Override
    public List<JobFeedbackPo> fetchTop(String jobClientNodeGroup, int top) {
        Query<JobFeedbackPo> query = createQuery(jobClientNodeGroup);
        query.order("gmtCreated").limit(top);
        return query.asList();
    }
}
