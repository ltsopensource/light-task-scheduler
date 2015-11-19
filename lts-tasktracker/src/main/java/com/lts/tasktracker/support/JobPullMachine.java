package com.lts.tasktracker.support;

import com.lts.core.constant.Constants;
import com.lts.core.constant.EcTopic;
import com.lts.core.exception.JobTrackerNotFoundException;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.protocol.JobProtos;
import com.lts.core.protocol.command.JobPullRequest;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;
import com.lts.remoting.exception.RemotingCommandFieldCheckException;
import com.lts.remoting.protocol.RemotingCommand;
import com.lts.tasktracker.domain.TaskTrackerApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用来向JobTracker去取任务
 * 1. 会订阅JobTracker的可用,不可用消息主题的订阅
 * 2. 只有当JobTracker可用的时候才会去Pull任务
 * 3. Pull只是会给JobTracker发送一个通知
 * Robert HG (254963746@qq.com) on 3/25/15.
 */
public class JobPullMachine {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPullMachine.class.getSimpleName());

    // 定时检查TaskTracker是否有空闲的线程，如果有，那么向JobTracker发起任务pull请求
    private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private TaskTrackerApplication application;
    private Runnable runnable;
    private int jobPullFrequency;

    public JobPullMachine(final TaskTrackerApplication application) {
        this.application = application;
        this.jobPullFrequency = application.getConfig().getParameter(Constants.JOB_PULL_FREQUENCY, Constants.DEFAULT_JOB_PULL_FREQUENCY);

        application.getEventCenter().subscribe(
                new EventSubscriber(JobPullMachine.class.getSimpleName().concat(application.getConfig().getIdentity()),
                        new Observer() {
                            @Override
                            public void onObserved(EventInfo eventInfo) {
                                if (EcTopic.JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
                                    // JobTracker 可用了
                                    start();
                                } else if (EcTopic.NO_JOB_TRACKER_AVAILABLE.equals(eventInfo.getTopic())) {
                                    stop();
                                }
                            }
                        }), EcTopic.JOB_TRACKER_AVAILABLE, EcTopic.NO_JOB_TRACKER_AVAILABLE);
        this.runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!start.get()) {
                        return;
                    }
                    sendRequest();
                } catch (Exception e) {
                    LOGGER.error("Job pull machine run error!", e);
                }
            }
        };
    }

    private void start() {
        try {
            if (start.compareAndSet(false, true)) {
                if (scheduledFuture == null) {
                    scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(runnable, 1, jobPullFrequency, TimeUnit.SECONDS);
                    // 5s 检查一次是否有空余线程
                }
                LOGGER.info("Start job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Start job pull machine failed!", t);
        }
    }

    private void stop() {
        try {
            if (start.compareAndSet(true, false)) {
//                scheduledFuture.cancel(true);
//                SCHEDULED_CHECKER.shutdown();
                LOGGER.info("Stop job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop job pull machine failed!", t);
        }
    }

    /**
     * 发送Job pull 请求
     */
    private void sendRequest() throws RemotingCommandFieldCheckException {
        int availableThreads = application.getRunnerPool().getAvailablePoolSize();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("current availableThreads:{}", availableThreads);
        }
        if (availableThreads == 0) {
            return;
        }
        JobPullRequest requestBody = application.getCommandBodyWrapper().wrapper(new JobPullRequest());
        requestBody.setAvailableThreads(availableThreads);
        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_PULL.code(), requestBody);

        try {
            RemotingCommand responseCommand = application.getRemotingClient().invokeSync(request);
            if (responseCommand == null) {
                LOGGER.warn("job pull request failed! response command is null!");
                return;
            }
            if (JobProtos.ResponseCode.JOB_PULL_SUCCESS.code() == responseCommand.getCode()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("job pull request success!");
                }
                return;
            }
            LOGGER.warn("job pull request failed! response command is null!");
        } catch (JobTrackerNotFoundException e) {
            LOGGER.warn("no job tracker available!");
        }
    }
}
