package com.lts.job.queue;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.util.ConcurrentHashSet;

import java.util.Set;

/**
 * Created by hugui on 5/19/15.
 */
public class JobQueueFactory {

    private static final ExtensionLoader<JobQueue> loader = ExtensionLoader.getExtensionLoader(JobQueue.class);
    private static final Set<JobQueue> connected = new ConcurrentHashSet<JobQueue>();

    public static JobQueue getJobQueue(Config config) {
        String extName = config.getParameter(Constants.JOB_QUEUE_KEY,
                loader.getDefaultExtensionName());
        JobQueue jobQueue = loader.getExtension(extName);
        if (!connected.contains(jobQueue)) {
            jobQueue.connect(config);
            connected.add(jobQueue);
        }
        return jobQueue;
    }
}
