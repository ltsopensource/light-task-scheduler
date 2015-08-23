package com.lts.tasktracker.monitor;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lts.core.cluster.Config;
import com.lts.core.commons.file.FileUtils;
import com.lts.core.commons.utils.*;
import com.lts.core.constant.Constants;
import com.lts.core.domain.TaskTrackerMI;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.tasktracker.domain.TaskTrackerApplication;

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
import java.util.concurrent.atomic.AtomicLong;

/**
 * 主要用来监控TaskTracker的压力
 * 1. 任务执行量，任务执行成功数，任务执行失败数
 * 2. FailStore 容量
 * 3. 内存占用情况
 * 定时向 monitor 发送，方便生成图表在LTS-Admin查看，预警等
 *
 * @author Robert HG (254963746@qq.com) on 8/21/15.
 */
public class Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);
    // 执行成功的个数
    private AtomicLong successNum = new AtomicLong(0);
    // 执行失败的个数
    private AtomicLong failedNum = new AtomicLong(0);
    // 总的运行时间
    private AtomicLong totalRunningTime = new AtomicLong(0);

    private ScheduledExecutorService reportScheduleExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> reportScheduledFuture;
    private ScheduledExecutorService pressureRecordScheduleExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pressureRecordScheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    // 这里面保存发送失败的，不过有个最大限制，防止内存爆掉
    private final List<TaskTrackerMI> mis = new ArrayList<TaskTrackerMI>();
    private List<TaskTrackerMI> toSendMis = new ArrayList<TaskTrackerMI>();
    private final int MAX_RETRY_RETAIN = 500;
    private final int BATCH_REPORT_SIZE = 10;
    private Config config;
    private Runtime runtime = Runtime.getRuntime();
    private String monitorURL;

    public Monitor(TaskTrackerApplication application) {
        this.config = application.getConfig();
        monitorURL = config.getParameter("lts.monitor.url");
        if (StringUtils.isEmpty(monitorURL)) {
            return;
        }
        start();
    }

    public void start() {
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
                pressureRecordScheduledFuture = pressureRecordScheduleExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TaskTrackerMI mi = new TaskTrackerMI();
                            mi.setSuccessNum(successNum.getAndSet(0));
                            mi.setFailedNum(failedNum.getAndSet(0));
                            mi.setTotalRunningTime(totalRunningTime.getAndSet(0));
                            long seconds = SystemClock.now() / 1000;
                            long residue = seconds % 5;
                            seconds = seconds - residue;        // 所有都向下取整，保证是5的倍数
                            mi.setTimestamp(seconds * 1000);
                            mi.setFailStoreSize(getFailStoreSize());
                            setMemoryInfo(mi);
                            mis.add(mi);

                        } catch (Throwable t) {
                            LOGGER.error("Pressure record failed.", t);
                        }
                    }
                }, 5, 5, TimeUnit.SECONDS);     // 5s 查看一下当前节点的压力

                LOGGER.info("Monitor start succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("Monitor start failed.", e);
        }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                reportScheduledFuture.cancel(true);
                reportScheduleExecutor.shutdown();
                pressureRecordScheduledFuture.cancel(true);
                pressureRecordScheduleExecutor.shutdown();
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
        params.put("nodeGroup", config.getNodeGroup());
        params.put("identity", config.getIdentity());
        int toIndex = 0;
        int size = toSendMis.size();
        try {
            for (int i = 0; i <= size / BATCH_REPORT_SIZE; i++) {
                List<TaskTrackerMI> taskTrackerMIs = BatchUtils.getBatchList(i, BATCH_REPORT_SIZE, toSendMis);
                if (CollectionUtils.isNotEmpty(taskTrackerMIs)) {
                    params.put("mis", JSONUtils.toJSONString(taskTrackerMIs));
                    try {
                        if (send(params)) {
                            toIndex = toIndex + CollectionUtils.sizeOf(taskTrackerMIs);
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
        String monitorUrl = config.getParameter("lts.monitor.url");
        if (StringUtils.isEmpty(monitorUrl)) {
            return false;
        }
        String result = WebUtils.doPost(monitorUrl + Constants.TASK_TRACKER_MONITOR_INFO_ADD_URL, params, 3000, 6000);
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
     * FailStore占用情况
     */
    private long getFailStoreSize() {
        String failStore = config.getParameter("job.fail.store", "leveldb");
        String path = config.getFailStorePath().concat(failStore).concat("/").concat(config.getIdentity());
        return FileUtils.getSize(new File(path));
    }

    /**
     * 设置内存占用信息
     */
    private void setMemoryInfo(TaskTrackerMI mi) {
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

    public void increaseSuccessNum() {
        successNum.incrementAndGet();
    }

    public void increaseFailedNum() {
        failedNum.incrementAndGet();
    }

    public void addRunningTime(Long time) {
        totalRunningTime.addAndGet(time);
    }

}
