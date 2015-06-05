package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.ExecutingJobQueue;
import com.lts.job.queue.ExecutingJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoExecutingJobQueueFactory implements ExecutingJobQueueFactory {
    @Override
    public ExecutingJobQueue getQueue(Config config) {
        return new MongoExecutingJobQueue(config);
    }
}
