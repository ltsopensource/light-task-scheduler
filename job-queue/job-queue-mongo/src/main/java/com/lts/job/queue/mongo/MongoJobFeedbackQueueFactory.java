package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.JobFeedbackQueueFactory;

/**
 * mongo 实现
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobFeedbackQueueFactory implements JobFeedbackQueueFactory{

    @Override
    public JobFeedbackQueue getJobFeedbackQueue(Config config) {
        return new MongoJobFeedbackQueue(config);
    }
}
