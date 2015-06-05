package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.JobFeedbackQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlJobFeedbackQueueFactory implements JobFeedbackQueueFactory {
    @Override
    public JobFeedbackQueue getQueue(Config config) {
        return new MysqlJobFeedbackQueue(config);
    }
}
