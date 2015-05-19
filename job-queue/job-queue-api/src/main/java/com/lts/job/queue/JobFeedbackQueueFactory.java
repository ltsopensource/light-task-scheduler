package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.extension.Adaptive;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.extension.SPI;
import com.lts.job.core.util.ConcurrentHashSet;

import java.util.Set;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI("mongo")
public interface JobFeedbackQueueFactory {

    @Adaptive("job.queue")
    JobFeedbackQueue getJobFeedbackQueue(Config config);
}
