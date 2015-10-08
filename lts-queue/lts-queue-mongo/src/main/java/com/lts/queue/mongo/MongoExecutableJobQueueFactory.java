package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.queue.ExecutableJobQueue;
import com.lts.queue.ExecutableJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoExecutableJobQueueFactory implements ExecutableJobQueueFactory {
    @Override
    public ExecutableJobQueue getQueue(Config config) {
        return new MongoExecutableJobQueue(config);
    }
}
