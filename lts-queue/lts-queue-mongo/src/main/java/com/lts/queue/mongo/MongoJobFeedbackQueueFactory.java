package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.queue.JobFeedbackQueue;
import com.lts.queue.JobFeedbackQueueFactory;

/**
 * mongo 实现
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobFeedbackQueueFactory implements JobFeedbackQueueFactory {
    @Override
    public JobFeedbackQueue getQueue(Config config) {
        return new MongoJobFeedbackQueue(config);
    }
}
