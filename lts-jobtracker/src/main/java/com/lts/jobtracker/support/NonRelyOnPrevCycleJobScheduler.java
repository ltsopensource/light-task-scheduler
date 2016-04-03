package com.lts.jobtracker.support;

import com.lts.core.commons.utils.DateUtils;
import com.lts.core.constant.Constants;
import com.lts.core.exception.LtsRuntimeException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.CronExpressionUtils;
import com.lts.core.support.JobUtils;
import com.lts.core.support.bean.BeanCopier;
import com.lts.core.support.bean.BeanCopierFactory;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.queue.domain.JobPo;
import com.lts.store.jdbc.exception.DupEntryException;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class NonRelyOnPrevCycleJobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonRelyOnPrevCycleJobScheduler.class);
    private JobTrackerAppContext appContext;
    private static final BeanCopier<JobPo, JobPo> BEAN_COPIER = BeanCopierFactory.createCopier(JobPo.class, JobPo.class, true);

    public NonRelyOnPrevCycleJobScheduler(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    public void addScheduleJobForOneHour(JobPo jobPo) {
        if (jobPo.isCron()) {
            addCronJobForOneHour(jobPo);
        } else if (jobPo.isRepeatable()) {
            addRepeatJobForOneHour(jobPo);
        } else {
            throw new LtsRuntimeException("Only For Cron Or Repeat Job Now");
        }
    }

    /**
     * 为当前时间以后的一个小时时间添加任务
     */
    private void addCronJobForOneHour(final JobPo finalJobPo) {
        // deepCopy
        JobPo jobPo = new JobPo();
        BEAN_COPIER.copyProps(finalJobPo, jobPo);

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
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                    jobPo.setJobId(JobUtils.generateJobId());
                    jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtils.format(nextTriggerTime, "MMdd-HHmmSS"));
                    jobPo.setInternalExtParam(Constants.ONCE, Boolean.TRUE.toString());
                    try {
                        appContext.getExecutableJobQueue().add(jobPo);
                    } catch (DupEntryException e) {
                        LOGGER.warn("Cron Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                                jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                    }
                } else {
                    stop = true;
                }
            }
            timeAfter = nextTriggerTime;
        }
    }

    private void addRepeatJobForOneHour(JobPo finalJobPo) {
        // deepCopy
        JobPo jobPo = new JobPo();
        BEAN_COPIER.copyProps(finalJobPo, jobPo);

        Long repeatInterval = jobPo.getRepeatInterval();
        Integer repeatCount = jobPo.getRepeatCount();
        Long firstTriggerTime = jobPo.getTriggerTime();

        Date now = new Date();
        long afterOneHour = DateUtils.addHour(now, 1).getTime();
        boolean stop = false;
        int repeatedCount = 0;
        while (!stop) {
            Long nextTriggerTime = firstTriggerTime + repeatedCount * repeatInterval;

            if (nextTriggerTime <= afterOneHour &&
                    (repeatCount == -1 || repeatedCount <= repeatCount)) {
                // 添加任务
                jobPo.setTriggerTime(nextTriggerTime);
                jobPo.setJobId(JobUtils.generateJobId());
                jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtils.format(new Date(nextTriggerTime), "MMdd-HHmmSS"));
                jobPo.setRepeatedCount(repeatedCount);
                jobPo.setInternalExtParam(Constants.ONCE, Boolean.TRUE.toString());
                try {
                    appContext.getExecutableJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.warn("Repeat Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                            jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                }
                repeatedCount++;
            } else {
                stop = true;
            }
        }
    }
}
