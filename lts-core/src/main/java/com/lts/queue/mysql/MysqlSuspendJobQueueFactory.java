package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.queue.CronJobQueue;
import com.lts.queue.SuspendJobQueue;
import com.lts.queue.SuspendJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlSuspendJobQueueFactory implements SuspendJobQueueFactory {
    @Override
    public SuspendJobQueue getQueue(Config config) {
        return new MysqlSuspendJobQueue(config);
    }
}
