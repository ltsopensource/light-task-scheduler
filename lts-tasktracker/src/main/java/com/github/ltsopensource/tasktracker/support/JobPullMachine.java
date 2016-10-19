package com.github.ltsopensource.tasktracker.support;

import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.EcTopic;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.exception.JobTrackerNotFoundException;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.protocol.JobProtos;
import com.github.ltsopensource.core.protocol.command.JobPullRequest;
import com.github.ltsopensource.ec.EventInfo;
import com.github.ltsopensource.ec.EventSubscriber;
import com.github.ltsopensource.ec.Observer;
import com.github.ltsopensource.jvmmonitor.JVMConstants;
import com.github.ltsopensource.jvmmonitor.JVMMonitor;
import com.github.ltsopensource.remoting.exception.RemotingCommandFieldCheckException;
import com.github.ltsopensource.remoting.protocol.RemotingCommand;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

import java.math.BigDecimal;
import java.math.MathContext;
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
 *
 * @author Robert HG (254963746@qq.com) on 3/25/15.
 */
public class JobPullMachine {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobPullMachine.class.getSimpleName());

    // 定时检查TaskTracker是否有空闲的线程，如果有，那么向JobTracker发起任务pull请求
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-JobPullMachine-Executor", true));
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private TaskTrackerAppContext appContext;
    private Runnable worker;
    private int jobPullFrequency;
    // 是否启用机器资源检查
    private boolean machineResCheckEnable = false;

    public JobPullMachine(final TaskTrackerAppContext appContext) {
        this.appContext = appContext;
        this.jobPullFrequency = appContext.getConfig().getParameter(ExtConfig.JOB_PULL_FREQUENCY, Constants.DEFAULT_JOB_PULL_FREQUENCY);

        this.machineResCheckEnable = appContext.getConfig().getParameter(ExtConfig.LB_MACHINE_RES_CHECK_ENABLE, false);

        appContext.getEventCenter().subscribe(
                new EventSubscriber(JobPullMachine.class.getSimpleName().concat(appContext.getConfig().getIdentity()),
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
        this.worker = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!start.get()) {
                        return;
                    }
                    if (!isMachineResEnough()) {
                        // 如果机器资源不足,那么不去取任务
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
                    scheduledFuture = executorService.scheduleWithFixedDelay(worker, jobPullFrequency * 1000, jobPullFrequency * 1000, TimeUnit.MILLISECONDS);
                }
                LOGGER.info("Start Job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Start Job pull machine failed!", t);
        }
    }

    private void stop() {
        try {
            if (start.compareAndSet(true, false)) {
//                scheduledFuture.cancel(true);
//                executorService.shutdown();
                LOGGER.info("Stop Job pull machine success!");
            }
        } catch (Throwable t) {
            LOGGER.error("Stop Job pull machine failed!", t);
        }
    }

    /**
     * 发送Job pull 请求
     */
    private void sendRequest() throws RemotingCommandFieldCheckException {
        int availableThreads = appContext.getRunnerPool().getAvailablePoolSize();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("current availableThreads:{}", availableThreads);
        }
        if (availableThreads == 0) {
            return;
        }
        JobPullRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobPullRequest());
        requestBody.setAvailableThreads(availableThreads);
        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.JOB_PULL.code(), requestBody);

        try {
            RemotingCommand responseCommand = appContext.getRemotingClient().invokeSync(request);
            if (responseCommand == null) {
                LOGGER.warn("Job pull request failed! response command is null!");
                return;
            }
            if (JobProtos.ResponseCode.JOB_PULL_SUCCESS.code() == responseCommand.getCode()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Job pull request success!");
                }
                return;
            }
            LOGGER.warn("Job pull request failed! response command is null!");
        } catch (JobTrackerNotFoundException e) {
            LOGGER.warn("no job tracker available!");
        }
    }

    /**
     * 查看当前机器资源是否足够
     */
    private boolean isMachineResEnough() {

        if (!machineResCheckEnable) {
            // 如果没有启用,直接返回
            return true;
        }

        boolean enough = true;

        try {
            // 1. Cpu usage
            Double maxCpuTimeRate = appContext.getConfig().getParameter(ExtConfig.LB_CPU_USED_RATE_MAX, 90d);
            Object processCpuTimeRate = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_THREAD_NAME, "ProcessCpuTimeRate");
            if (processCpuTimeRate != null) {
                Double cpuRate = Double.valueOf(processCpuTimeRate.toString()) / (Constants.AVAILABLE_PROCESSOR * 1.0);
                if (cpuRate >= maxCpuTimeRate) {
                    LOGGER.info("Pause Pull, CPU USAGE is " + String.format("%.2f", cpuRate) + "% >= " + String.format("%.2f", maxCpuTimeRate) + "%");
                    enough = false;
                    return false;
                }
            }

            // 2. Memory usage
            Double maxMemoryUsedRate = appContext.getConfig().getParameter(ExtConfig.LB_MEMORY_USED_RATE_MAX, 90d);
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();

            Double memoryUsedRate = new BigDecimal(usedMemory / (maxMemory*1.0), new MathContext(4)).doubleValue();

            if (memoryUsedRate >= maxMemoryUsedRate) {
                LOGGER.info("Pause Pull, MEMORY USAGE is " + memoryUsedRate + " >= " + maxMemoryUsedRate);
                enough = false;
                return false;
            }
            enough = true;
            return true;
        } catch (Exception e) {
            LOGGER.warn("Check Machine Resource error", e);
            return true;
        } finally {
            Boolean machineResEnough = appContext.getConfig().getInternalData(Constants.MACHINE_RES_ENOUGH, true);
            if (machineResEnough != enough) {
                appContext.getConfig().setInternalData(Constants.MACHINE_RES_ENOUGH, enough);
            }
        }
    }
}
