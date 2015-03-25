package com.lts.job.core.logger;

import com.lts.job.core.repository.po.JobLogPo;

/**
 * Created by hugui on 3/24/15.
 */
public interface LtsLogger {

    public void log(JobLogPo log);

}
