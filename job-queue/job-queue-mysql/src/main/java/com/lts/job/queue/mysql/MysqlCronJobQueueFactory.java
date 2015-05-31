package com.lts.job.queue.mysql;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.CronJobQueue;
import com.lts.job.queue.CronJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlCronJobQueueFactory implements CronJobQueueFactory {
    @Override
    public CronJobQueue getQueue(Config config) {
        return new MysqlCronJobQueue(config);
    }
}
