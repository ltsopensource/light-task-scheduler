package com.lts.job.biz.logger.console;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.commons.utils.JSONUtils;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class ConsoleJobLogger implements JobLogger {

    private Logger LOGGER = LoggerFactory.getLogger(ConsoleJobLogger.class.getSimpleName());

    @Override
    public void log(JobLogPo jobLogPo) {
        LOGGER.info(JSONUtils.toJSONString(jobLogPo));
    }

    @Override
    public void log(BizLogPo bizLogPo) {
        LOGGER.info("BIZ LOG : {}", JSONUtils.toJSONString(bizLogPo));
    }
}
