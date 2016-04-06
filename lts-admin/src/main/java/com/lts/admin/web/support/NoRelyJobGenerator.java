package com.lts.admin.web.support;

import com.lts.admin.cluster.BackendAppContext;
import com.lts.queue.domain.JobPo;
import com.lts.queue.support.NonRelyJobUtils;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/6/16.
 */
public class NoRelyJobGenerator {

    private BackendAppContext appContext;
    private int scheduleIntervalMinute;

    public NoRelyJobGenerator(BackendAppContext appContext) {
        this.appContext = appContext;
        this.scheduleIntervalMinute = this.appContext.getConfig().getParameter("jobtracker.nonRelyOnPrevCycleJob.schedule.interval.minute", 10);

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
