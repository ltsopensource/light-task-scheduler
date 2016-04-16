package com.github.ltsopensource.biz.logger.mongo;

import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.biz.logger.JobLoggerFactory;
import com.github.ltsopensource.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 12/27/15.
 */
public class MongoJobLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new MongoJobLogger(config);
    }
}
