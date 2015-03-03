package com.lts.job.common.repository;

import com.google.code.morphia.query.Query;
import com.lts.job.common.repository.po.JobFeedbackQueuePo;
import com.lts.job.store.mongo.AbstractMongoRepository;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/23/14.
 */
public class JobFeedbackQueueMongoRepository extends AbstractMongoRepository<JobFeedbackQueuePo> {

    public void delJobFeedback(String id) {
        Query<JobFeedbackQueuePo> query = createQuery().field("id").equal(id);
        ds.delete(query);
    }

    public List<JobFeedbackQueuePo> get(int offset, int limit){
        Query<JobFeedbackQueuePo> query = createQuery();
        query.order("gmtCreated").offset(offset).limit(limit);
        return query.asList();
    }

}
