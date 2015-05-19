package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI("mongo")
public interface JobQueueFactory {

    @Adaptive("job.queue")
    JobQueue getJobQueue(Config config);
}
