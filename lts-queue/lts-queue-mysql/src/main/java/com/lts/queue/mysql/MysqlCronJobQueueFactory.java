package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.queue.CronJobQueue;
import com.lts.queue.CronJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlCronJobQueueFactory implements CronJobQueueFactory {
    @Override
    public CronJobQueue getQueue(Config config) {
        return new MysqlCronJobQueue(config);
    }
}
