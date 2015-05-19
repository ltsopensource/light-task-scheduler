package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.JobQueue;
import com.lts.job.queue.JobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class MongoJobQueueFactory implements JobQueueFactory {
    @Override
    public JobQueue getJobQueue(Config config) {
        return new MongoJobQueue(config);
    }
}
