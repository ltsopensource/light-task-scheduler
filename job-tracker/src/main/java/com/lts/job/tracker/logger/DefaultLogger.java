package com.lts.job.tracker.logger;

import com.lts.job.core.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class DefaultLogger implements JobLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogger.class.getSimpleName());

    @Override
    public void log(JobLogPo log) {
        LOGGER.info(JSONUtils.toJSONString(log));
    }
}
