package com.lts.job.queue.mongo;

import com.google.code.morphia.query.Query;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.domain.JobFeedbackPo;
import com.lts.job.store.mongo.AbstractMongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

/**
 * mongo 实现
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobFeedbackQueue extends AbstractMongoRepository<JobFeedbackPo> implements JobFeedbackQueue {

    public MongoJobFeedbackQueue(Config config) {
        super(config);
        doCreateTable();
    }

    @Override
    public void add(List<JobFeedbackPo> jobFeedbackPo) {
        ds.save(jobFeedbackPo);
    }

    @Override
    public void remove(String id) {
        Query<JobFeedbackPo> query = createQuery().field("id").equal(id);
        ds.delete(query);
    }

    public long count() {
        Query<JobFeedbackPo> query = createQuery();
        return ds.getCount(query);
    }

    @Override
    public List<JobFeedbackPo> fetch(int offset, int limit) {
        Query<JobFeedbackPo> query = createQuery();
        query.order("gmtCreated").offset(offset).limit(limit);
        return query.asList();
    }

    protected void doCreateTable() {
        // 创建 JobFeedbackPo 索引
        Class<?> clazz = JobFeedbackPo.class;
        DBCollection dbCollection = ds.getCollection(clazz);
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        if (CollectionUtils.isEmpty(indexInfo)) {
            ds.ensureIndex(clazz, "idx_gmtCreated", "gmtCreated", false, true);
        }
    }
}
