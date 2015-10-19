package com.lts.web.support;

import com.lts.core.commons.utils.DateUtils;
import com.lts.core.commons.utils.QuietUtils;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.web.repository.mapper.*;
import com.lts.web.request.JVMDataRequest;
import com.lts.web.request.MonitorDataRequest;
import com.lts.web.request.NodeOnOfflineLogRequest;
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
public class LtsAdminDataCleaner implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LtsAdminDataCleaner.class);

    @Autowired
    private TaskTrackerMonitorRepo taskTrackerMonitorRepo;
    @Autowired
    private JobTrackerMonitorRepo jobTrackerMonitorDataRepo;
    @Autowired
    private NodeOnOfflineLogRepo nodeOnOfflineLogRepo;
    @Autowired
    private JVMInfoRepo jvmInfoRepo;
    @Autowired
    private JVMGCRepo jvmgcRepo;
    @Autowired
    private JVMThreadRepo jvmThreadRepo;
    @Autowired
    private JVMMemoryRepo jvmMemoryRepo;

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
            LOGGER.info("LtsAdminDataCleaner start succeed ");
        }
    }

    private void clean() {
        //  1. 清除TaskTracker JobTracker的统计数据(3天之前的)
        final MonitorDataRequest request = new MonitorDataRequest();
        request.setEndTime(DateUtils.addDay(new Date(), -3).getTime());

        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                taskTrackerMonitorRepo.delete(request);
            }
        });

        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                jobTrackerMonitorDataRepo.delete(request);
            }
        });

        // 2. 清除30天以前的节点上下线日志
        final NodeOnOfflineLogRequest nodeOnOfflineLogRequest = new NodeOnOfflineLogRequest();
        nodeOnOfflineLogRequest.setEndLogTime(DateUtils.addDay(new Date(), -30));

        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                nodeOnOfflineLogRepo.delete(nodeOnOfflineLogRequest);
            }
        });

        // 3. 清除3天前的JVM监控信息
        final JVMDataRequest jvmDataRequest = new JVMDataRequest();
        jvmDataRequest.setEndTime(DateUtils.addDay(new Date(), -3).getTime());
        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                jvmInfoRepo.delete(jvmDataRequest);
            }
        });
        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                jvmgcRepo.delete(jvmDataRequest);
            }
        });
        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                jvmThreadRepo.delete(jvmDataRequest);
            }
        });
        QuietUtils.doWithWarn(new QuietUtils.Callable() {
            @Override
            public void call() throws Exception {
                jvmMemoryRepo.delete(jvmDataRequest);
            }
        });

        LOGGER.info("Clean monitor data succeed ");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
