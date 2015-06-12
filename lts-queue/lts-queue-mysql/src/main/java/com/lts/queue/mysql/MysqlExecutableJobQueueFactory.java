package com.lts.queue.mysql;

import com.lts.core.cluster.Config;
import com.lts.queue.ExecutableJobQueue;
import com.lts.queue.ExecutableJobQueueFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutableJobQueueFactory implements ExecutableJobQueueFactory {
    @Override
    public ExecutableJobQueue getQueue(Config config) {
        return new MysqlExecutableJobQueue(config);
    }
}
