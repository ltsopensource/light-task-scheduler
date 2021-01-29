package com.github.ltsopensource.queue.support;

import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.CronExpressionUtils;
import com.github.ltsopensource.core.support.JobUtils;
import com.github.ltsopensource.queue.CronJobQueue;
import com.github.ltsopensource.queue.ExecutableJobQueue;
import com.github.ltsopensource.queue.RepeatJobQueue;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

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
                        jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
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

        Long repeatInterval = jobPo.getRepeatInterval();
        Integer repeatCount = jobPo.getRepeatCount();

        long endTime = DateUtils.addMinute(lastGenerateTime, scheduleIntervalMinute).getTime();
        if (endTime <= firstTriggerTime) {
            return;
        }
        
        // 计算出已经生成的可执行job 起始repeatedCount
        long initTime = lastGenerateTime.getTime();
        int repeatedCount = Long.valueOf((initTime - firstTriggerTime) / jobPo.getRepeatInterval()).intValue();
        if (repeatedCount <= 0) {
            repeatedCount = 1;
        }

        final JobPo repeatJobPjo = repeatJobQueue.getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId());

        boolean stop = false;
        while (!stop) {
            final Long nextTriggerTime = firstTriggerTime + (repeatedCount - 1) * repeatInterval;

            if (repeatJobPjo.getLastGenerateTriggerTime() == null || repeatJobPjo.getLastGenerateTriggerTime() == 0) {
                // 说明是第一次生成executable job，默认第一次为0，如果第一次设置为当前时间，那么有可能第一次triggertime小于当前时间，导致少生成一次job
                initTime = 0;
            }

            //这里也要大于上次的generateTiggerTime，防止上次生成的job正在执行时，这回又生成成功可执行job，因为这里是通过数据库唯一索引去重的
            if ((nextTriggerTime > initTime) && (nextTriggerTime <= endTime)
                    && (repeatCount == -1 || repeatedCount <= repeatCount)) {
                // 添加任务
                jobPo.setTriggerTime(nextTriggerTime);
                jobPo.setJobId(JobUtils.generateJobId());
                jobPo.setTaskId(finalJobPo.getTaskId() + "_"
                        + DateUtils.format(new Date(nextTriggerTime), "MMdd-HHmmss"));
                jobPo.setRepeatedCount(repeatedCount);
                jobPo.setInternalExtParam(Constants.ONCE, Boolean.TRUE.toString());
                try {
                    jobPo.setInternalExtParam(Constants.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                    executableJobQueue.add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.warn("Repeat Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                            jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                }
                repeatedCount++;
            } else if (repeatedCount < repeatCount) {
                //前面的job已经生成过了，继续增加repeatedCount，生成下面的job
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
