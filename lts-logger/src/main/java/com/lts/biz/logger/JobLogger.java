package com.lts.biz.logger;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.web.response.PageResponse;

import java.util.List;

/**
 * 执行任务日志记录器
 *
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public void log(List<JobLogPo> jobLogPos);

    public PageResponse<JobLogPo> search(JobLoggerRequest request);
}