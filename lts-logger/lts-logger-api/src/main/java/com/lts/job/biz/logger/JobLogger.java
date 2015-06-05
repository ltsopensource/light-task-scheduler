package com.lts.job.biz.logger;

import com.lts.job.biz.logger.domain.BizLogPo;
import com.lts.job.biz.logger.domain.JobLogPo;
import com.lts.job.core.extension.SPI;

/**
 * 执行任务日志记录器
 *
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
@SPI("console")
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public void log(BizLogPo bizLogPo);

}