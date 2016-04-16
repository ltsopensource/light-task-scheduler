package com.github.ltsopensource.biz.logger;

import com.github.ltsopensource.biz.logger.domain.JobLogPo;
import com.github.ltsopensource.biz.logger.domain.LogType;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.support.JobDomainConverter;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.domain.JobPo;

/**
 * @author Robert HG (254963746@qq.com) on 4/6/16.
 */
public class JobLogUtils {

    public static void log(LogType logType, JobPo jobPo, JobLogger jobLogger) {
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(logType);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogger.log(jobLogPo);
    }
}
