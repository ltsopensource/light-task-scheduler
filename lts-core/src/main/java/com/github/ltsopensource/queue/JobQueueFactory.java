package com.github.ltsopensource.queue;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
@SPI(key = ExtConfig.JOB_QUEUE, dftValue = "mysql")
public interface JobQueueFactory {

    CronJobQueue getCronJobQueue(Config config);

    RepeatJobQueue getRepeatJobQueue(Config config);

    ExecutableJobQueue getExecutableJobQueue(Config config);

    ExecutingJobQueue getExecutingJobQueue(Config config);

    JobFeedbackQueue getJobFeedbackQueue(Config config);

    NodeGroupStore getNodeGroupStore(Config config);

    SuspendJobQueue getSuspendJobQueue(Config config);

    PreLoader getPreLoader(AppContext appContext);
}

