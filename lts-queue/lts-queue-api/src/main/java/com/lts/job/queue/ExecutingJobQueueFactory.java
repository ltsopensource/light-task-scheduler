package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
@SPI("mysql")
public interface ExecutingJobQueueFactory {

    @Adaptive("job.queue")
    ExecutingJobQueue getQueue(Config config);
}