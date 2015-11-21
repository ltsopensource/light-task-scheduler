package com.lts.biz.logger.console;

import com.lts.biz.logger.JobLogger;
import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.core.json.JSON;
import com.lts.web.response.PageResponse;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class ConsoleJobLogger implements JobLogger {

    private Logger LOGGER = LoggerFactory.getLogger(ConsoleJobLogger.class.getSimpleName());

    @Override
    public void log(JobLogPo jobLogPo) {
        LOGGER.info(JSON.toJSONString(jobLogPo));
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        for (JobLogPo jobLogPo : jobLogPos) {
            log(jobLogPo);
        }
    }

    @Override
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {
        throw new UnsupportedOperationException("Console logger dose not support this operation!");
    }

}
