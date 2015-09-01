package com.lts.web.support;

import com.lts.core.commons.utils.DateUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.web.repository.JobTrackerMonitorDataRepository;
import com.lts.web.repository.TaskTrackerMonitorDataRepository;
import com.lts.web.request.MonitorDataRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时清除 monitor 数据
 *
 * @author Robert HG (254963746@qq.com) on 8/23/15.
 */
@Component
public class MonitorDataCleaner implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorDataCleaner.class);
    @Autowired
    private TaskTrackerMonitorDataRepository taskTrackerMonitorDataRepository;
    @Autowired
    private JobTrackerMonitorDataRepository jobTrackerMonitorDataRepository;
    private ScheduledExecutorService cleanExecutor = Executors.newSingleThreadScheduledExecutor();

    private AtomicBoolean start = new AtomicBoolean(false);

    public void start() {
        if (start.compareAndSet(false, true)) {
            cleanExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        clean();
                    } catch (Throwable t) {
                        LOGGER.error("Clean monitor data error ", t);
                    }
                }
            }, 1, 24, TimeUnit.HOURS);
        }
    }

    private void clean() {
        //  1. 清除TaskTracker JobTracker的统计数据(5天之前的)
        Long endTime = DateUtils.addDay(new Date(), -5).getTime();
        MonitorDataRequest request = new MonitorDataRequest();
        request.setEndTime(endTime);
        taskTrackerMonitorDataRepository.delete(request);
        jobTrackerMonitorDataRepository.delete(request);

        LOGGER.info("Clean monitor data before {} succeed ", DateUtils.formatYMD_HMS(new Date(endTime)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
