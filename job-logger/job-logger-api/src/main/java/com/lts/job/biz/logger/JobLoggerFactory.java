package com.lts.job.biz.logger;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.extension.ExtensionLoader;
import com.lts.job.core.util.ConcurrentHashSet;

import java.util.Set;

/**
 * Created by hugui on 5/19/15.
 */
public class JobLoggerFactory {

    private static final ExtensionLoader<JobLogger> loader =
            ExtensionLoader.getExtensionLoader(JobLogger.class);
    private static final Set<JobLogger> inited = new ConcurrentHashSet<JobLogger>();

    public static JobLogger getLogger(Config config) {
        String extName = config.getParameter(Constants.JOB_LOGGER_KEY,
                loader.getDefaultExtensionName());
        JobLogger jobLogger = loader.getExtension(extName);
        if (!inited.contains(jobLogger)) {
            jobLogger.init(config);
            inited.add(jobLogger);
        }
        return jobLogger;
    }

}
