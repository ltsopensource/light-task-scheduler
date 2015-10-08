package com.lts.jobtracker.support.checker;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.JSONUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.queue.domain.JobPo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!application.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            fix();
                        } catch (Throwable t) {
                            LOGGER.error(t.getMessage(), t);
                        }
                    }
                }, 30, 60, TimeUnit.SECONDS);// 3分钟执行一次
            }
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
            List<JobPo> deadJobPo = application.getExecutableJobQueue().getDeadJob(nodeGroup, SystemClock.now() - MAX_TIME_OUT);
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
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executable dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executable dead job checker stop failed!", t);
        }
    }
}
