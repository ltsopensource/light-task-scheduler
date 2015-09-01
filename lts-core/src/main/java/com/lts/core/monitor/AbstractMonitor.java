package com.lts.core.monitor;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lts.core.Application;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.*;
import com.lts.core.constant.Constants;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;

import java.io.File;
import java.io.IOException;
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

    private Runtime runtime = Runtime.getRuntime();

    protected Config config;
    protected String monitorSite;

    private ScheduledExecutorService reportScheduleExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> reportScheduledFuture;
    private ScheduledExecutorService monitorDataCollectScheduleExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> monitorDataCollectScheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    // 这里面保存发送失败的，不过有个最大限制，防止内存爆掉
    private final List<MonitorData> mis = new ArrayList<MonitorData>();
    private List<MonitorData> toSendMis = new ArrayList<MonitorData>();
    private final int MAX_RETRY_RETAIN = 500;
    private final int BATCH_REPORT_SIZE = 10;

    public AbstractMonitor(Application application) {
        config = application.getConfig();
    }

    public final void start() {
        monitorSite = config.getParameter("lts.monitor.url");
        if (StringUtils.isEmpty(monitorSite)) {
            return;
        }
        try {
            if (start.compareAndSet(false, true)) {
                // 用来汇报数据
                reportScheduledFuture = reportScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            report();
                        } catch (Throwable t) {
                            LOGGER.error("Report monitor data failed.", t);
                        }
                    }
                }, 1, 1, TimeUnit.MINUTES);

                // 用来记录每段时间的压力
                monitorDataCollectScheduledFuture = monitorDataCollectScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MonitorData monitorData = collectMonitorData();
                            long seconds = SystemClock.now() / 1000;
                            long residue = seconds % 5;
                            seconds = seconds - residue;        // 所有都向下取整，保证是5的倍数
                            monitorData.setTimestamp(seconds * 1000);
                            setMemoryInfo(monitorData);
                            mis.add(monitorData);
                        } catch (Throwable t) {
                            LOGGER.error("MonitorData collect failed.", t);
                        }
                    }
                }, 5, 5, TimeUnit.SECONDS);     // 5s 查看一下当前节点的压力

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
                reportScheduledFuture.cancel(true);
                reportScheduleExecutor.shutdown();
                monitorDataCollectScheduledFuture.cancel(true);
                monitorDataCollectScheduleExecutor.shutdown();
                LOGGER.info("Monitor stop succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("Monitor stop failed.", e);
        }
    }

    private void report() {
        // Send monitor data
        Map<String, String> params = new HashMap<String, String>();
        synchronized (mis) {
            toSendMis.addAll(mis);
            mis.clear();
        }
        if (toSendMis.size() == 0) {
            return;
        }
        params.put("nodeType", getNodeType().name());
        params.put("nodeGroup", config.getNodeGroup());
        params.put("identity", config.getIdentity());
        int toIndex = 0;
        int size = toSendMis.size();
        try {
            for (int i = 0; i <= size / BATCH_REPORT_SIZE; i++) {
                List<MonitorData> monitorDataList = BatchUtils.getBatchList(i, BATCH_REPORT_SIZE, toSendMis);
                if (CollectionUtils.isNotEmpty(monitorDataList)) {
                    params.put("monitorData", JSONUtils.toJSONString(monitorDataList));
                    try {
                        if (send(params)) {
                            toIndex = toIndex + CollectionUtils.sizeOf(monitorDataList);
                            LOGGER.info("Report monitor data success ");
                        } else {
                            LOGGER.warn("Report monitor data failed, send later ,please check the LTS-Admin is available");
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Report monitor data failed, send later, please check the LTS-Admin is available : " + e.getMessage());
                        break;
                    }
                }
            }
        } finally {
            // to delete
            if (toIndex == 0) {
                // to nothing
            } else if (size == toIndex) {
                toSendMis.clear();
            } else {
                toSendMis = toSendMis.subList(toIndex + 1, size);
            }
        }
        // check size
        size = toSendMis.size();
        if (size > MAX_RETRY_RETAIN) {
            // delete the oldest
            toSendMis = toSendMis.subList(size - MAX_RETRY_RETAIN, size);
        }
    }

    private boolean send(Map<String, String> params) throws IOException {
        String result = WebUtils.doPost(monitorSite + Constants.MONITOR_DATA_ADD_URL, params, 3000, 6000);
        if (StringUtils.isNotEmpty(result)) {
            try {
                JSONObject json = JSONUtils.parseObject(result);
                if (json.getBoolean("success")) {
                    return true;
                }
            } catch (JSONException e) {
                LOGGER.error("Monitor report result : \n" + result);
            }
        }
        return false;
    }

    /**
     * 设置内存占用信息
     */
    private void setMemoryInfo(MonitorData mi) {
        // 最大内存
        long maxMemory = runtime.maxMemory();
        // 已分配内存
        long allocatedMemory = runtime.totalMemory();
        // 已分配内存中的剩余内存
        long freeMemory = runtime.freeMemory();
        // 总的空闲内存
        long totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);

        mi.setMaxMemory(maxMemory);
        mi.setAllocatedMemory(allocatedMemory);
        mi.setFreeMemory(freeMemory);
        mi.setTotalFreeMemory(totalFreeMemory);
    }

    /**
     * FailStore占用情况
     */
    protected long getFailStoreSize() {
        String failStore = config.getParameter("job.fail.store", "leveldb");
        String path = config.getFailStorePath().concat(failStore).concat("/").concat(config.getIdentity());
        return FileUtils.getSize(new File(path));
    }

}
