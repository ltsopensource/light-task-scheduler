package com.lts.jobtracker.support;

import com.lts.core.commons.utils.Callable;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.DateUtils;
import com.lts.core.constant.Constants;
import com.lts.core.exception.LtsRuntimeException;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.CronExpressionUtils;
import com.lts.core.support.JobUtils;
import com.lts.core.support.NodeShutdownHook;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.queue.domain.JobPo;
import com.lts.store.jdbc.exception.DupEntryException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 4/2/16.
 */
public class NonRelyOnPrevCycleJobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonRelyOnPrevCycleJobScheduler.class);
    private JobTrackerAppContext appContext;
    private int scheduleIntervalMinute;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean running = new AtomicBoolean(false);

    private AtomicBoolean start = new AtomicBoolean(false);

    public NonRelyOnPrevCycleJobScheduler(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.scheduleIntervalMinute = this.appContext.getConfig().getParameter("jobtracker.nonRelyOnPrevCycleJob.schedule.interval.minute", 10);

        NodeShutdownHook.registerHook(appContext, this.getClass().getSimpleName(), new Callable() {
            @Override
            public void call() throws Exception {
                stop();
            }
        });
    }

    public void start() {
        if (!start.compareAndSet(false, true)) {
            return;
        }
        try {
            executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(NonRelyOnPrevCycleJobScheduler.class.getSimpleName(), true));
            this.scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (running.compareAndSet(false, true)) {
                            try {
                                schedule();
                            } finally {
                                running.set(false);
                            }
                        }
                    } catch (Throwable t) {
                        LOGGER.error("Error On Schedule", t);
                    }
                }
            }, 10, (scheduleIntervalMinute - 1) * 60, TimeUnit.SECONDS);
        } catch (Throwable t) {
            LOGGER.error("Scheduler Start Error", t);
        }
    }

    public void stop() {
        if (!start.compareAndSet(true, false)) {
            return;
        }
        try {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
            if (executorService != null) {
                executorService.shutdownNow();
                executorService = null;
            }
        } catch (Throwable t) {
            LOGGER.error("Scheduler Stop Error", t);
        }
    }

    private void schedule() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========= Scheduler start =========");
        }

        Date now = new Date();
        Date checkTime = DateUtils.addMinute(now, 10);
        //  cron任务
        while (true) {
            List<JobPo> jobPos = appContext.getCronJobQueue().getNeedGenerateJobPos(checkTime.getTime(), 10);
            if (CollectionUtils.sizeOf(jobPos) == 0) {
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("========= CronJob size[{}] =========", CollectionUtils.sizeOf(jobPos));
            }
            for (JobPo jobPo : jobPos) {
                Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
                if (lastGenerateTriggerTime == null) {
                    lastGenerateTriggerTime = new Date().getTime();
                }
                addCronJobForInterval(jobPo, new Date(lastGenerateTriggerTime));
            }
        }

        // repeat 任务
        while (true) {
            List<JobPo> jobPos = appContext.getRepeatJobQueue().getNeedGenerateJobPos(checkTime.getTime(), 10);
            if (CollectionUtils.sizeOf(jobPos) == 0) {
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("========= Repeat size[{}] =========", CollectionUtils.sizeOf(jobPos));
            }
            for (JobPo jobPo : jobPos) {
                Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
                if (lastGenerateTriggerTime == null) {
                    lastGenerateTriggerTime = new Date().getTime();
                }
                addRepeatJobForInterval(jobPo, new Date(lastGenerateTriggerTime));
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========= Scheduler End =========");
        }
    }

    public void addScheduleJobForOneHour(JobPo jobPo) {
        if (jobPo.isCron()) {
            addCronJobForInterval(jobPo, new Date());
        } else if (jobPo.isRepeatable()) {
            addRepeatJobForInterval(jobPo, new Date());
        } else {
            throw new LtsRuntimeException("Only For Cron Or Repeat Job Now");
        }
    }

    /**
     * 生成一个小时的任务
     */
    private void addCronJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
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
        appContext.getCronJobQueue().updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add CronJob {} to {}", jobPo, DateUtils.formatYMD_HMS(new Date(endTime)));
        }
    }

    private void addRepeatJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
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
        // 更新时间
        appContext.getRepeatJobQueue().updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add RepeatJob {} to {}", jobPo, DateUtils.formatYMD_HMS(new Date(endTime)));
        }
    }
}
