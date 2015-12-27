package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.queue.ExecutingJobQueue;
import com.lts.queue.ExecutingJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutingJobQueueFactory implements ExecutingJobQueueFactory {
    @Override
    public ExecutingJobQueue getQueue(Config config) {
        return new MysqlExecutingJobQueue(config);
    }
}
