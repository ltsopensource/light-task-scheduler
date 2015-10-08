package com.lts.core.monitor;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lts.core.Application;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.*;
import com.lts.core.constant.Constants;
import com.lts.core.domain.monitor.MonitorData;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.jvmmonitor.JVMCollector;
import com.lts.jvmmonitor.JVMMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/30/15.
 */
public abstract class AbstractMonitor implements Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    protected Config config;
    protected String monitorSite;

    private ScheduledExecutorService monitorDataCollectScheduleExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> monitorDataCollectScheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    // 这里面保存发送失败的，不过有个最大限制，防止内存爆掉
    private List<MonitorData> toSendMonitorDataList = new ArrayList<MonitorData>();
    private final static int MAX_RETRY_RETAIN = 500;
    private final static int BATCH_REPORT_SIZE = 10;

    public AbstractMonitor(Application application) {
        config = application.getConfig();
    }

    private final int INTERVAL = 60;    // 1分钟
    private boolean jvmInfoSendSuccess = false;     // 是否JVMInfo信息是否发送成功

    public final void start() {
        monitorSite = config.getParameter("lts.monitor.url");
        if (StringUtils.isEmpty(monitorSite)) {
            return;
        }
        try {
            if (start.compareAndSet(false, true)) {

                // 用来记录每段时间的压力
                monitorDataCollectScheduledFuture = monitorDataCollectScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MonitorData monitorData = collectMonitorData();
                            long seconds = SystemClock.now() / 1000;
                            long residue = seconds % INTERVAL;
                            seconds = seconds - residue;        // 所有都向下取整，保证是60的倍数
                            monitorData.setTimestamp(seconds * 1000);
                            // JVM monitor
                            monitorData.setJvmMonitorData(JVMCollector.collect());
                            // report
                            report(monitorData);

                            // 检查是否发送成功
                            checkSendJVMInfo();

                        } catch (Throwable t) {
                            LOGGER.error("MonitorData collect failed.", t);
                        }
                    }
                }, 5, INTERVAL, TimeUnit.SECONDS);     // 60s 查看一下当前节点的压力

                // 启动JVM监控
                JVMMonitor.start();

                // 首次启动发送JVMInfo给LTS-Admin
                checkSendJVMInfo();

                LOGGER.info("Monitor start succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("Monitor start failed.", e);
        }
    }

    /**
     * 用来收集数据
     */
    protected abstract MonitorData collectMonitorData();

    protected abstract NodeType getNodeType();

    public final void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                monitorDataCollectScheduledFuture.cancel(true);
                monitorDataCollectScheduleExecutor.shutdown();
                JVMMonitor.stop();
                LOGGER.info("Monitor stop succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("Monitor stop failed.", e);
        }
    }

    private void report(MonitorData monitorData) {
        // Send monitor data
        toSendMonitorDataList.add(monitorData);

        int toIndex = 0;
        int size = toSendMonitorDataList.size();
        try {
            for (int i = 0; i <= size / BATCH_REPORT_SIZE; i++) {
                List<MonitorData> subList = BatchUtils.getBatchList(i, BATCH_REPORT_SIZE, toSendMonitorDataList);
                if (CollectionUtils.isNotEmpty(subList)) {
                    try {
                        if (send(getPostParam(subList), Constants.MONITOR_DATA_ADD_URL)) {
                            toIndex = toIndex + CollectionUtils.sizeOf(subList);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Report monitor data success ");
                            }
                        } else {
                            LOGGER.warn("Report monitor data failed(" + monitorSite + "), send later ,please check the LTS-Admin is available");
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Report monitor data failed(" + monitorSite + "), send later, please check the LTS-Admin is available : " + e.getMessage());
                        break;
                    }
                }
            }
        } finally {
            // to delete
            if (toIndex == 0) {
                // to nothing
            } else if (size == toIndex) {
                toSendMonitorDataList.clear();
            } else {
                toSendMonitorDataList = toSendMonitorDataList.subList(toIndex + 1, size);
            }
        }
        // check size
        size = toSendMonitorDataList.size();
        if (size > MAX_RETRY_RETAIN) {
            // delete the oldest
            toSendMonitorDataList = toSendMonitorDataList.subList(size - MAX_RETRY_RETAIN, size);
        }
    }

    private boolean send(Map<String, String> params, String api) throws Exception {
        String url = monitorSite + api;
        String result = WebUtils.doPost(url, params, 3000, 6000);
        if (StringUtils.isNotEmpty(result)) {
            try {
                JSONObject json = JSONUtils.parseObject(result);
                if (json.getBoolean("success")) {
                    return true;
                } else {
                    throw new Exception("Monitor(" + url + ") report result : \n" + result);
                }
            } catch (JSONException e) {
                throw new Exception("Monitor(" + url + ") report result : \n" + result);
            }
        }
        return false;
    }

    /**
     * FailStore占用情况
     */
    protected long getFailStoreSize() {
        String failStore = config.getParameter("job.fail.store", "leveldb");
        String path = config.getFailStorePath().concat(failStore).concat("/").concat(config.getIdentity());
        return FileUtils.getSize(new File(path));
    }

    private Map<String, String> getPostParam(Object data) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("nodeType", getNodeType().name());
        params.put("nodeGroup", config.getNodeGroup());
        params.put("identity", config.getIdentity());
        params.put("data", JSONUtils.toJSONString(data));
        return params;
    }

    /**
     * 首次启动发送JVMInfo给LTS-Admin
     */
    private void checkSendJVMInfo() {
        if (jvmInfoSendSuccess) {
            return;
        }
        Map<String, Object> infoMap = JVMCollector.getJVMInfo();
        try {
            if (send(getPostParam(infoMap), Constants.MONITOR_JVM_INFO_DATA_ADD_URL)) {
                jvmInfoSendSuccess = true;
            }
        } catch (Exception e) {
            LOGGER.warn("Report JVMInfo data failed(" + monitorSite + "), send later, please check the LTS-Admin is available : " + e.getMessage());
        }
    }

}
