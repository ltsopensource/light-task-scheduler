package com.github.ltsopensource.core.monitor;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.domain.monitor.MData;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.support.NodeShutdownHook;
import com.github.ltsopensource.jvmmonitor.JVMMonitor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/30/15.
 */
public abstract class AbstractMStatReporter implements MStatReporter {

    protected final Logger LOGGER = LoggerFactory.getLogger(AbstractMStatReporter.class);

    protected AppContext appContext;
    protected Config config;

    private ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-Monitor-data-collector", true));
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);

    public AbstractMStatReporter(AppContext appContext) {
        this.appContext = appContext;
        this.config = appContext.getConfig();
    }

    public final void start() {

        // 启动JVM监控
        JVMMonitor.start();

        final MStatReportWorker worker = new MStatReportWorker(appContext, this);

        try {
            if (!config.getParameter(ExtConfig.M_STAT_REPORTER_CLOSED, false)) {
                if (start.compareAndSet(false, true)) {
                    scheduledFuture = executor.scheduleWithFixedDelay(worker, 1, 1, TimeUnit.SECONDS);
                    LOGGER.info("MStatReporter start succeed.");
                }
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter start failed.", e);
        }

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                worker.run();
            }
        });
    }

    /**
     * 用来收集数据
     */
    protected abstract MData collectMData();

    protected abstract NodeType getNodeType();

    public final void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                executor.shutdown();
                JVMMonitor.stop();
                LOGGER.info("MStatReporter stop succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter stop failed.", e);
        }
    }

}
