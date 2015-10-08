package com.lts.biz.logger.console;

import com.lts.biz.logger.JobLogger;
import com.lts.biz.logger.JobLoggerFactory;
import com.lts.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class ConsoleLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new ConsoleJobLogger();
    }
}
