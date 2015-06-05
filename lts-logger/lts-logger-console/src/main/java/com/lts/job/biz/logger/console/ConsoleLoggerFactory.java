package com.lts.job.biz.logger.console;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.JobLoggerFactory;
import com.lts.job.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class ConsoleLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new ConsoleJobLogger();
    }
}
