package com.github.ltsopensource.jobtracker.complete.retry;

import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.spi.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 6/28/16.
 */
@SPI(key = ExtConfig.JOB_RETRY_TIME_GENERATOR, dftValue = "default")
public interface JobRetryTimeGenerator {

    /**
     * 得到任务重试的下一次时间
     *
     * @param retryTimes 已经重试的次数
     * @param retryInterval 重试间隔
     */
    long getNextRetryTriggerTime(Job job, int retryTimes, int retryInterval);
}
