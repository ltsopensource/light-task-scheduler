package com.github.ltsopensource.admin.web.support;

import com.github.ltsopensource.admin.cluster.BackendAppContext;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.queue.support.NonRelyJobUtils;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/6/16.
 */
public class NoRelyJobGenerator {

    private BackendAppContext appContext;
    private int scheduleIntervalMinute;

    public NoRelyJobGenerator(BackendAppContext appContext) {
        this.appContext = appContext;
        this.scheduleIntervalMinute = this.appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE, 10);

    }

    public void generateCronJobForInterval(final JobPo jobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addCronJobForInterval(appContext.getExecutableJobQueue(), appContext.getCronJobQueue(),
                scheduleIntervalMinute, jobPo, lastGenerateTime);
    }

    public void generateRepeatJobForInterval(final JobPo jobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addRepeatJobForInterval(appContext.getExecutableJobQueue(), appContext.getRepeatJobQueue(),
                scheduleIntervalMinute, jobPo, lastGenerateTime);
    }

}
