package com.lts.job.biz.logger.console;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.core.cluster.Config;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class ConsoleLogger implements JobLogger {

    private Logger LOGGER;

    @Override
    public void init(Config config) {
        LOGGER = LoggerFactory.getLogger(ConsoleLogger.class.getSimpleName());
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        LOGGER.info(JSONUtils.toJSONString(jobLogPo));
    }

    @Override
    public void log(BizLogPo bizLogPo) {
        LOGGER.info("BIZ LOG : {}", JSONUtils.toJSONString(bizLogPo));
    }
}
