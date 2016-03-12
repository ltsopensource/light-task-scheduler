package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.spi.SPI;
import com.lts.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
@SPI(key = SpiExtensionKey.JOB_QUEUE, dftValue = "mysql")
public interface JobQueueFactory {

    CronJobQueue getCronJobQueue(Config config);

    ExecutableJobQueue getExecutableJobQueue(Config config);

    ExecutingJobQueue getExecutingJobQueue(Config config);

    JobFeedbackQueue getJobFeedbackQueue(Config config);

    NodeGroupStore getNodeGroupStore(Config config);

    SuspendJobQueue getSuspendJobQueue(Config config);

    PreLoader getPreLoader(Config config);
}

