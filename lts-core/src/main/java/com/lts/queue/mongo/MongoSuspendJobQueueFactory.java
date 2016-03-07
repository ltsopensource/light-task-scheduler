package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.queue.*;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoSuspendJobQueueFactory implements SuspendJobQueueFactory {
    @Override
    public SuspendJobQueue getQueue(Config config) {
        return new MongoSuspendJobQueue(config);
    }
}
