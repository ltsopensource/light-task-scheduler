package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.ExecutableJobQueue;
import com.lts.job.queue.ExecutableJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoExecutableJobQueueFactory implements ExecutableJobQueueFactory {
    @Override
    public ExecutableJobQueue getQueue(Config config) {
        return new MongoExecutableJobQueue(config);
    }
}
