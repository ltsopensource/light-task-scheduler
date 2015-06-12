package com.lts.biz.logger.mongo;

import com.lts.biz.logger.JobLogger;
import com.lts.core.cluster.Config;
import com.lts.biz.logger.JobLoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public class MongoJobLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new MongoJobLogger(config);
    }
}
