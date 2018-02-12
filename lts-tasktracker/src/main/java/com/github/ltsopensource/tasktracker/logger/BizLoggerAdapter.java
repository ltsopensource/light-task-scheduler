package com.github.ltsopensource.tasktracker.logger;

import com.github.ltsopensource.core.domain.JobMeta;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public abstract class BizLoggerAdapter implements BizLogger {

    private JobMeta jobMeta;

    public void setJobMeta(JobMeta jobMeta) {
        this.jobMeta = jobMeta;
    }

    protected JobMeta getJobMeta() {
        return jobMeta;
    }

}
