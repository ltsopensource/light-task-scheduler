package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.domain.JobQueueRequest;
import com.lts.job.core.domain.PageRequest;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.queue.JobQueue;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.store.mongo.MongoRepository;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 6/7/15.
 */
public abstract class AbstractMongoJobQueue extends MongoRepository implements JobQueue{

    public AbstractMongoJobQueue(Config config) {
        super(config);
    }

    @Override
    public PageResponse<JobPo> pageSelect(JobQueueRequest request) {
        return null;
    }

    @Override
    public boolean selectiveUpdate(JobQueueRequest request) {
        return false;
    }
}
