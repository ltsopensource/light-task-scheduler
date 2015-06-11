package com.lts.job.biz.logger;

import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.biz.logger.domain.JobLoggerRequest;
import com.lts.job.core.domain.PageResponse;
import com.lts.job.core.extension.SPI;

import java.util.List;

/**
 * 执行任务日志记录器
 *
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
@SPI("console")
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public PageResponse<JobLogPo> search(JobLoggerRequest request);
}