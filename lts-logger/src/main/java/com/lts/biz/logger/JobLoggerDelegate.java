package com.lts.biz.logger;

import com.lts.biz.logger.domain.JobLogPo;
import com.lts.biz.logger.domain.JobLoggerRequest;
import com.lts.core.cluster.Config;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.spi.ServiceLoader;
import com.lts.web.response.PageResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内部根据用户参数决定是否采用延迟批量刷盘的策略,来提高吞吐量
 * 批量刷盘有两种情况:
 * 1. 内存的日志量超过了设置的阀值
 * 2. 每3S检查一次内存中是否有日志,如果有就那么刷盘
 *
 * @author Robert HG (254963746@qq.com) on 10/2/15.
 */
public class JobLoggerDelegate implements JobLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLoggerDelegate.class);

    // 3S 检查输盘一次日志
    private int flushPeriod;

    private JobLogger jobLogger;
    private boolean lazyLog = false;
    private ScheduledExecutorService executor;
    @SuppressWarnings("unused")
	private ScheduledFuture<?> scheduledFuture;
    private BlockingQueue<JobLogPo> memoryQueue;
    // 日志批量刷盘数量
    private int batchFlushSize = 100;
    private int overflowSize = 10000;
    // 内存中最大的日志量阀值
    private int maxMemoryLogSize;
    private AtomicBoolean flushing = new AtomicBoolean(false);

    public JobLoggerDelegate(Config config) {
        JobLoggerFactory jobLoggerFactory = ServiceLoader.load(JobLoggerFactory.class, config);
        jobLogger = jobLoggerFactory.getJobLogger(config);
        lazyLog = config.getParameter(Constants.LAZY_JOB_LOGGER, false);
        if (lazyLog) {

            // 无界Queue
            memoryQueue = new LinkedBlockingQueue<JobLogPo>();
            maxMemoryLogSize = config.getParameter(Constants.LAZY_JOB_LOGGER_MEM_SIZE, 1000);
            flushPeriod = config.getParameter(Constants.LAZY_JOB_LOGGER_CHECK_PERIOD, 3);

            executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LazyJobLogger"));
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (flushing.compareAndSet(false, true)) {
                            checkAndFlush();
                        }
                    } catch (Throwable t) {
                        LOGGER.error("CheckAndFlush log error", t);
                    }
                }
            }, flushPeriod, flushPeriod, TimeUnit.SECONDS);

        }
    }

    /**
     * 检查内存中是否有日志,如果有就批量刷盘
     */
    private void checkAndFlush() {
        try {
            int nowSize = memoryQueue.size();
            if (nowSize == 0) {
                return;
            }
            List<JobLogPo> batch = new ArrayList<JobLogPo>();
            for (int i = 0; i < nowSize; i++) {
                JobLogPo jobLogPo = memoryQueue.poll();
                batch.add(jobLogPo);

                if (batch.size() >= batchFlushSize) {
                    flush(batch);
                }
            }
            if (batch.size() > 0) {
                flush(batch);
            }

        } finally {
            flushing.compareAndSet(true, false);
        }
    }

    private void checkOverflowSize() {
        if (memoryQueue.size() > overflowSize) {
            throw new JobLogException("Memory Log size is " + memoryQueue.size() + " , please check the JobLogger is available");
        }
    }

    private void flush(List<JobLogPo> batch) {
        boolean flushSuccess = false;
        try {
            jobLogger.log(batch);
            flushSuccess = true;
        } finally {
            if (!flushSuccess) {
                memoryQueue.addAll(batch);
            }
            batch.clear();
        }
    }

    /**
     * 检查内存中的日志量是否超过阀值,如果超过需要批量刷盘日志
     */
    private void checkCapacity() {
        if (memoryQueue.size() > maxMemoryLogSize) {
            // 超过阀值,需要批量刷盘
            if (flushing.compareAndSet(false, true)) {
                // 这里可以采用new Thread, 因为这里只会同时new一个
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkAndFlush();
                        } catch (Throwable t) {
                            LOGGER.error("Capacity full flush error", t);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void log(JobLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        if (lazyLog) {
            checkOverflowSize();
            memoryQueue.offer(jobLogPo);
            checkCapacity();
        } else {
            jobLogger.log(jobLogPo);
        }
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
        if (lazyLog) {
            checkOverflowSize();
            for (JobLogPo jobLogPo : jobLogPos) {
                memoryQueue.offer(jobLogPo);
            }
            // checkCapacity
            checkCapacity();
        } else {
            jobLogger.log(jobLogPos);
        }
    }

    @Override
    public PageResponse<JobLogPo> search(JobLoggerRequest request) {
        return jobLogger.search(request);
    }

}
