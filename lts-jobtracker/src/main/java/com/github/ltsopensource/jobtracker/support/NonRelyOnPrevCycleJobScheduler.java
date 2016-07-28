package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.exception.LtsRuntimeException;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.NodeShutdownHook;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.queue.support.NonRelyJobUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 仅用于不依赖上一周期的任务生成器
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
        this.scheduleIntervalMinute = this.appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE, 10);

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
                if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
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
                if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
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

    private void addCronJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addCronJobForInterval(appContext.getExecutableJobQueue(), appContext.getCronJobQueue(),
                scheduleIntervalMinute, finalJobPo, lastGenerateTime);
    }

    private void addRepeatJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addRepeatJobForInterval(appContext.getExecutableJobQueue(), appContext.getRepeatJobQueue(),
                scheduleIntervalMinute, finalJobPo, lastGenerateTime);
    }
}
