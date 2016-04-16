package com.github.ltsopensource.jobtracker.support.checker;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;
import com.github.ltsopensource.queue.domain.JobPo;

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

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-ExecutableJobQueue-Fix-Executor", true));

    private JobTrackerAppContext appContext;

    public ExecutableDeadJobChecker(JobTrackerAppContext appContext) {
        this.appContext = appContext;
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
                            if (!appContext.getRegistryStatMonitor().isAvailable()) {
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
        Set<String> nodeGroups = appContext.getTaskTrackerManager().getNodeGroups();
        if (CollectionUtils.isEmpty(nodeGroups)) {
            return;
        }
        for (String nodeGroup : nodeGroups) {
            List<JobPo> deadJobPo = appContext.getExecutableJobQueue().getDeadJob(nodeGroup, SystemClock.now() - MAX_TIME_OUT);
            if (CollectionUtils.isNotEmpty(deadJobPo)) {
                for (JobPo jobPo : deadJobPo) {
                    appContext.getExecutableJobQueue().resume(jobPo);
                    LOGGER.info("Fix executable job : {} ", JSON.toJSONString(jobPo));
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
