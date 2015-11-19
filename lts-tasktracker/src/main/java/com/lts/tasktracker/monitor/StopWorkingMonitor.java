package com.lts.tasktracker.monitor;

import com.lts.core.constant.Constants;
import com.lts.core.constant.EcTopic;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;
import com.lts.tasktracker.domain.TaskTrackerApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 当TaskTracker和JobTracker断开超过了一段时间，TaskTracker立即停止当前的所有任务
 *
 * @author Robert HG (254963746@qq.com) on 9/9/15.
 */
public class StopWorkingMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopWorkingMonitor.class);
    private TaskTrackerApplication application;
    private AtomicBoolean start = new AtomicBoolean(false);
    private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private String ecSubscriberName = StopWorkingMonitor.class.getSimpleName();
    private EventSubscriber eventSubscriber;
    private Long offlineTimestamp = null;

    public StopWorkingMonitor(TaskTrackerApplication application) {
        this.application = application;
    }

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                eventSubscriber = new EventSubscriber(ecSubscriberName, new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        // 当JobTracker可用的时候，置为null, 重新统计
                        offlineTimestamp = null;
                    }
                });
                application.getEventCenter().subscribe(eventSubscriber, EcTopic.JOB_TRACKER_AVAILABLE);

                scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (offlineTimestamp == null && application.getRemotingClient().isServerEnable()) {
                                offlineTimestamp = SystemClock.now();
                            }

                            if (offlineTimestamp != null &&
                                    SystemClock.now() - offlineTimestamp > Constants.TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
                                // 停止所有任务
                                application.getRunnerPool().stopWorking();
                                offlineTimestamp = null;
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Check ", t);
                        }
                    }
                }, 5, 5, TimeUnit.SECONDS);
                LOGGER.info("start succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("start failed ", t);
        }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                SCHEDULED_CHECKER.shutdown();

                application.getEventCenter().unSubscribe(EcTopic.JOB_TRACKER_AVAILABLE, eventSubscriber);

                LOGGER.info("stop succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("stop failed ", t);
        }
    }

}
