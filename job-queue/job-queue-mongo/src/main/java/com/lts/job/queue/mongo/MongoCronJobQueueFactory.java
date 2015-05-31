package com.lts.job.queue.mongo;

import com.lts.job.core.cluster.Config;
import com.lts.job.queue.CronJobQueue;
import com.lts.job.queue.CronJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoCronJobQueueFactory implements CronJobQueueFactory {
    @Override
    public CronJobQueue getQueue(Config config) {
        return new MongoCronJobQueue(config);
    }
}
