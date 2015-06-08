package com.lts.job.tracker.support.checker;

import com.lts.job.core.logger.Logger;
import com.lts.job.core.logger.LoggerFactory;
import com.lts.job.core.util.CollectionUtils;
import com.lts.job.core.util.DateUtils;
import com.lts.job.core.util.JSONUtils;
import com.lts.job.queue.domain.JobPo;
import com.lts.job.tracker.domain.JobTrackerApplication;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * to fix the executable dead job
 *
 * @author Robert HG (254963746@qq.com) on 6/3/15.
 */
public class ExecutableDeadJobChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableDeadJobChecker.class);

    // 1 分钟还锁着的，说明是有问题的
    private static final long MAX_TIME_OUT = 60 * 1000;

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private JobTrackerApplication application;

    public ExecutableDeadJobChecker(JobTrackerApplication application) {
        this.application = application;
    }

    private volatile boolean start;
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start) {
                return;
            }
            scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        fix();
                    } catch (Throwable t) {
                        LOGGER.error(t.getMessage(), t);
                    }
                }
            }, 30, 60, TimeUnit.SECONDS);// 3分钟执行一次
            start = true;

            LOGGER.info("Executable dead job checker started!");
        } catch (Throwable t) {
            LOGGER.info("Executable dead job checker start failed!");
        }
    }

    /**
     * fix the job that running is true and gmtModified too old
     */
    private void fix() {
        Set<String> nodeGroups = application.getTaskTrackerManager().getNodeGroups();
        if (CollectionUtils.isEmpty(nodeGroups)) {
            return;
        }
        for (String nodeGroup : nodeGroups) {
            List<JobPo> deadJobPo = application.getExecutableJobQueue().getDeadJob(nodeGroup, DateUtils.currentTimeMillis() - MAX_TIME_OUT);
            if (CollectionUtils.isNotEmpty(deadJobPo)) {
                for (JobPo jobPo : deadJobPo) {
                    application.getExecutableJobQueue().resume(jobPo);
                    LOGGER.info("Fix executable job : {} ", JSONUtils.toJSONString(jobPo));
                }
            }
        }
    }

    public void stop() {
        try {
            if (start) {
                start = false;
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executable dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executable dead job checker stop failed!", t);
        }
    }
}
