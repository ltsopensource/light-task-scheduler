package com.lts.biz.logger;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.LogType;
import com.lts.core.constant.Level;
import com.lts.core.support.JobDomainConverter;
import com.lts.core.support.SystemClock;
import com.lts.queue.domain.JobPo;

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
