package com.lts.queue.mongo;

import com.lts.core.cluster.Config;
import com.lts.queue.CronJobQueue;
import com.lts.queue.CronJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public class MongoCronJobQueueFactory implements CronJobQueueFactory {
    @Override
    public CronJobQueue getQueue(Config config) {
        return new MongoCronJobQueue(config);
    }
}
