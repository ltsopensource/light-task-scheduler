package com.github.ltsopensource.tasktracker.logger;

import com.github.ltsopensource.core.domain.JobMeta;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public abstract class BizLoggerAdapter implements BizLogger {

    private final ThreadLocal<JobMeta> jobMetaThreadLocal;

    public BizLoggerAdapter() {
        this.jobMetaThreadLocal = new ThreadLocal<JobMeta>();
    }

    public void setJobMeta(JobMeta jobMeta) {
        jobMetaThreadLocal.set(jobMeta);
    }

    public void removeJobMeta() {
        jobMetaThreadLocal.remove();
    }

    protected JobMeta getJobMeta() {
        return jobMetaThreadLocal.get();
    }

}
