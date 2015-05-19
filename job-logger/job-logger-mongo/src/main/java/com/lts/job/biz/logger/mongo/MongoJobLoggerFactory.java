package com.lts.job.biz.logger.mongo;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.JobLoggerFactory;
import com.lts.job.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class MongoJobLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new MongoJobLogger(config);
    }
}
