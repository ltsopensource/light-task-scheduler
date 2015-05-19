package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.util.ConcurrentHashSet;

import java.util.Set;

/**
 * Created by hugui on 5/19/15.
 */
public class JobFeedbackQueueFactory {

    private static final ExtensionLoader<JobFeedbackQueue> loader = ExtensionLoader.getExtensionLoader(JobFeedbackQueue.class);
    private static final Set<JobFeedbackQueue> connected = new ConcurrentHashSet<JobFeedbackQueue>();

    public static JobFeedbackQueue getJobFeedbackQueue(Config config) {
        String extName = config.getParameter(Constants.JOB_QUEUE_KEY,
                loader.getDefaultExtensionName());
        JobFeedbackQueue jobFeedbackQueue = loader.getExtension(extName);
        if(!connected.contains(jobFeedbackQueue)){
            jobFeedbackQueue.connect(config);
            connected.add(jobFeedbackQueue);
        }
        return jobFeedbackQueue;
    }
}
