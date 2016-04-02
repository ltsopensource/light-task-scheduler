package com.lts.jobtracker.support;

import com.lts.core.commons.utils.DateUtils;
import com.lts.core.exception.LtsRuntimeException;
import com.lts.core.support.CronExpressionUtils;
import com.lts.core.support.JobUtils;
import com.lts.core.support.bean.BeanCopier;
import com.lts.core.support.bean.BeanCopierFactory;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.queue.domain.JobPo;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class NonRelyOnPrevCycleJobScheduler {

    private JobTrackerAppContext appContext;
    private static final BeanCopier<JobPo, JobPo> beanCopier = BeanCopierFactory.getBeanCopier(JobPo.class, JobPo.class, true);

    public NonRelyOnPrevCycleJobScheduler(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    public void addScheduleJobForOneHour(JobPo jobPo) {
        if (jobPo.isCron()) {
            addCronJobForOneHour(jobPo);
        } else if (jobPo.isRepeatable()) {
            addRepeatJobForOneHour(jobPo);
        }
        throw new LtsRuntimeException();
    }

    /**
     * 为当前时间以后的一个小时时间添加任务
     */
    private void addCronJobForOneHour(final JobPo finalJobPo) {
        // deepCopy
        JobPo jobPo = new JobPo();
        beanCopier.copyProps(finalJobPo, jobPo);

        String cronExpression = jobPo.getCronExpression();
        Date now = new Date();
        long afterOneHour = DateUtils.addHour(now, 1).getTime();
        Date timeAfter = now;
        boolean stop = false;
        while (!stop) {
            Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronExpression, timeAfter);
            if (nextTriggerTime == null) {
                stop = true;
            } else {
                if (nextTriggerTime.getTime() <= afterOneHour) {
                    // 添加任务


                } else {
                    stop = true;
                }
            }
            timeAfter = nextTriggerTime;
        }
    }

    private void addRepeatJobForOneHour(JobPo jobPo) {

    }

    private void addJob(JobPo jobPo, long nextTriggerTime){
        jobPo.setJobId(JobUtils.generateJobId());

    }
}
