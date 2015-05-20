package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.JobQueue;
import com.lts.job.queue.JobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/20/15.
 */
public class MysqlJobQueueFactory implements JobQueueFactory {

    @Override
    public JobQueue getJobQueue(Config config) {
        return new MysqlJobQueue(config);
    }
}
