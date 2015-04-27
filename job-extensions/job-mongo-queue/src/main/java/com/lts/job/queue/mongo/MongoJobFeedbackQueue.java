package com.lts.job.queue.mongo;

import com.google.code.morphia.query.Query;
import com.lts.job.queue.mongo.store.AbstractMongoRepository;
import com.lts.job.queue.mongo.store.Config;
import com.lts.job.tracker.queue.JobFeedbackPo;
import com.lts.job.tracker.queue.JobFeedbackQueue;

import java.util.List;

/**
 * mongo 实现
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobFeedbackQueue extends AbstractMongoRepository<JobFeedbackPo> implements JobFeedbackQueue {

    public MongoJobFeedbackQueue(Config config) {
        super(config);
    }

    @Override
    public void add(List<JobFeedbackPo> jobFeedbackPo) {
        super.save(jobFeedbackPo);
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

}
