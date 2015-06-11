package com.lts.job.biz.logger.console;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.JobLoggerRequest;
import com.lts.job.core.commons.utils.JSONUtils;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;

import java.util.List;

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
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {
        throw new UnsupportedOperationException("Console logger dose not support this operation!");
    }

}
