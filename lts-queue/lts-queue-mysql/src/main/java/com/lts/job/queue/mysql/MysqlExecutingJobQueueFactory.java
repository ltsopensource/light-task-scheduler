package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.ExecutingJobQueue;
import com.lts.job.queue.ExecutingJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutingJobQueueFactory implements ExecutingJobQueueFactory {
    @Override
    public ExecutingJobQueue getQueue(Config config) {
        return new MysqlExecutingJobQueue(config);
    }
}
