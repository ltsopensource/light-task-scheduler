package com.lts.job.tracker.logger;

import com.lts.job.tracker.logger.domain.BizLogPo;
import com.lts.job.tracker.logger.domain.JobLogPo;

/**
 * 执行任务日志记录器
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public void log(BizLogPo bizLogPo);

}
