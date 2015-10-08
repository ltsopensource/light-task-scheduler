package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.queue.ExecutingJobQueue;
import com.lts.queue.ExecutingJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoExecutingJobQueueFactory implements ExecutingJobQueueFactory {
    @Override
    public ExecutingJobQueue getQueue(Config config) {
        return new MongoExecutingJobQueue(config);
    }
}
