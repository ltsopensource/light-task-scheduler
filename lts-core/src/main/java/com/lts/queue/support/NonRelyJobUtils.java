package com.lts.queue.support;

import com.lts.core.commons.utils.DateUtils;
import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.CronExpressionUtils;
import com.lts.core.support.JobUtils;
import com.lts.queue.CronJobQueue;
import com.lts.queue.ExecutableJobQueue;
import com.lts.queue.RepeatJobQueue;
import com.lts.queue.domain.JobPo;
import com.lts.store.jdbc.exception.DupEntryException;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/6/16.
 */
public class NonRelyJobUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonRelyJobUtils.class);

    /**
     * 生成一个小时的任务
     */
    public static void addCronJobForInterval(ExecutableJobQueue executableJobQueue,
                                             CronJobQueue cronJobQueue,
                                             int scheduleIntervalMinute,
                                             final JobPo finalJobPo,
                                             Date lastGenerateTime) {
        JobPo jobPo = JobUtils.copy(finalJobPo);

        String cronExpression = jobPo.getCronExpression();
        long endTime = DateUtils.addMinute(lastGenerateTime, scheduleIntervalMinute).getTime();
        Date timeAfter = lastGenerateTime;
        boolean stop = false;
        while (!stop) {
            Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronExpression, timeAfter);
            if (nextTriggerTime == null) {
                stop = true;
            } else {
                if (nextTriggerTime.getTime() <= endTime) {
                    // 添加任务
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                    jobPo.setJobId(JobUtils.generateJobId());
                    jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtils.format(nextTriggerTime, "MMdd-HHmmss"));
                    jobPo.setInternalExtParam(Constants.ONCE, Boolean.TRUE.toString());
                    try {
                        executableJobQueue.add(jobPo);
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
        cronJobQueue.updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add CronJob {} to {}", jobPo, DateUtils.formatYMD_HMS(new Date(endTime)));
        }
    }

    public static void addRepeatJobForInterval(
            ExecutableJobQueue executableJobQueue,
            RepeatJobQueue repeatJobQueue,
            int scheduleIntervalMinute, final JobPo finalJobPo, Date lastGenerateTime) {
        JobPo jobPo = JobUtils.copy(finalJobPo);
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(Constants.FIRST_FIRE_TIME));
        // 计算出应该重复的次数
        int repeatedCount = Long.valueOf((lastGenerateTime.getTime() - firstTriggerTime) / jobPo.getRepeatInterval()).intValue();

        Long repeatInterval = jobPo.getRepeatInterval();
        Integer repeatCount = jobPo.getRepeatCount();

        long endTime = DateUtils.addMinute(lastGenerateTime, scheduleIntervalMinute).getTime();
        boolean stop = false;
        while (!stop) {
            Long nextTriggerTime = firstTriggerTime + repeatedCount * repeatInterval;

            if (nextTriggerTime <= endTime &&
                    (repeatCount == -1 || repeatedCount <= repeatCount)) {
                // 添加任务
                jobPo.setTriggerTime(nextTriggerTime);
                jobPo.setJobId(JobUtils.generateJobId());
                jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtils.format(new Date(nextTriggerTime), "MMdd-HHmmss"));
                jobPo.setRepeatedCount(repeatedCount);
                jobPo.setInternalExtParam(Constants.ONCE, Boolean.TRUE.toString());
                try {
                    executableJobQueue.add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.warn("Repeat Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                            jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                }
                repeatedCount++;
            } else {
                stop = true;
            }
        }
        // 更新时间
        repeatJobQueue.updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add RepeatJob {} to {}", jobPo, DateUtils.formatYMD_HMS(new Date(endTime)));
        }
    }
}
