package com.github.ltsopensource.queue;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.core.commons.utils.Callable;
import com.github.ltsopensource.core.commons.utils.*;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.support.NodeShutdownHook;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.queue.domain.JobPo;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public abstract class AbstractPreLoader implements PreLoader {

    private int loadSize;
    // 预取阀值
    private double factor;

    private ConcurrentHashMap<String/*taskTrackerNodeGroup*/, JobPriorityBlockingDeque> JOB_MAP = new ConcurrentHashMap<String, JobPriorityBlockingDeque>();

    // 加载的信号
    private ConcurrentHashSet<String> LOAD_SIGNAL = new ConcurrentHashSet<String>();
    private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-PreLoader", true));
    @SuppressWarnings("unused")
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private String FORCE_PREFIX = "force_"; // 强制加载的信号
    private ConcurrentHashMap<String/*taskTrackerNodeGroup*/, AtomicBoolean/*是否在加载*/> LOADING = new ConcurrentHashMap<String, AtomicBoolean>();

    public AbstractPreLoader(final AppContext appContext) {
        if (start.compareAndSet(false, true)) {

            loadSize = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_PRELOADER_SIZE, 300);
            factor = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_PRELOADER_FACTOR, 0.2);
            long interval = appContext.getConfig().getParameter(ExtConfig.JOB_TRACKER_PRELOADER_SIGNAL_CHECK_INTERVAL, 100);

            scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    doLoad();
                }
            }, interval, interval, TimeUnit.MILLISECONDS);

            NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
                @Override
                public void call() throws Exception {
                    scheduledFuture.cancel(true);
                    LOAD_EXECUTOR_SERVICE.shutdown();
                    start.set(false);
                }
            });
        }
    }

    private void doLoad() {
        for (final String loadTaskTrackerNodeGroup : LOAD_SIGNAL) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AtomicBoolean loading = LOADING.get(loadTaskTrackerNodeGroup);
                    if (loading == null) {
                        loading = new AtomicBoolean(false);
                        AtomicBoolean _loading = LOADING.putIfAbsent(loadTaskTrackerNodeGroup, loading);
                        if (_loading != null) {
                            loading = _loading;
                        }
                    }
                    if (loading.compareAndSet(false, true)) {
                        try {
                            handleSignal(loadTaskTrackerNodeGroup);
                        } finally {
                            loading.compareAndSet(true, false);
                        }
                    }
                }
            }).start();
        }
    }

    private void handleSignal(String loadTaskTrackerNodeGroup) {
        // 是否是强制加载
        boolean force = false;
        if (loadTaskTrackerNodeGroup.startsWith(FORCE_PREFIX)) {
            loadTaskTrackerNodeGroup = loadTaskTrackerNodeGroup.replaceFirst(FORCE_PREFIX, "");
            force = true;
        }

        JobPriorityBlockingDeque queue = JOB_MAP.get(loadTaskTrackerNodeGroup);
        if (queue == null) {
            return;
        }
        int size = queue.size();
        if (force || (isInFactor(size))) {

            int needLoadSize = loadSize + size;
            if (force) {
                // 强制加载全量加载吧
                needLoadSize = loadSize;
            }
            // load
            PeriodUtils.start();
            List<JobPo> loads = null;
            try {
                loads = load(loadTaskTrackerNodeGroup, needLoadSize);
            } finally {
                PeriodUtils.end("AbstractPreLoader.load loadTaskTrackerNodeGroup:{},loadSide={}", loadTaskTrackerNodeGroup, needLoadSize);
            }
            // 加入到内存中
            if (CollectionUtils.isNotEmpty(loads)) {
                PeriodUtils.start();
                try {
                    for (JobPo load : loads) {
                        if (!queue.offer(load)) {
                            // 没有成功说明已经满了
                            if (force) {
                                // force场景，移除队列尾部的，插入新的
                                queue.pollLast();
                                queue.offer(load);
                            } else {
                                break;
                            }
                        }
                    }
                } finally {
                    PeriodUtils.end("AbstractPreLoader.offer loadTaskTrackerNodeGroup:{},loadSide={}", loadTaskTrackerNodeGroup, needLoadSize);
                }
            }
        }
        LOAD_SIGNAL.remove(loadTaskTrackerNodeGroup);
    }

    public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity) {
        while (true) {
            JobPo jobPo = get(taskTrackerNodeGroup);
            if (jobPo == null) {
                DotLogUtils.dot("Empty JobQueue, taskTrackerNodeGroup:{}, taskTrackerIdentity:{}", taskTrackerNodeGroup, taskTrackerIdentity);
                return null;
            }
            // update jobPo
            PeriodUtils.start();
            try {
                if (lockJob(taskTrackerNodeGroup, jobPo.getJobId(),
                        taskTrackerIdentity, jobPo.getTriggerTime(),
                        jobPo.getGmtModified())) {
                    jobPo.setTaskTrackerIdentity(taskTrackerIdentity);
                    jobPo.setIsRunning(true);
                    jobPo.setGmtModified(SystemClock.now());
                    return jobPo;
                }
            } finally {
                PeriodUtils.end("AbstractPreLoader.lockJob taskId:{}", jobPo.getTaskId());
            }
        }
    }

    @Override
    public void load(String taskTrackerNodeGroup) {
        if (StringUtils.isEmpty(taskTrackerNodeGroup)) {
            for (String key : JOB_MAP.keySet()) {
                LOAD_SIGNAL.add(FORCE_PREFIX + key);
            }
            return;
        }
        LOAD_SIGNAL.add(FORCE_PREFIX + taskTrackerNodeGroup);
    }

    @Override
    public void loadOne2First(String taskTrackerNodeGroup, String jobId) {
        JobPo jobPo = getJob(taskTrackerNodeGroup, jobId);
        if (jobPo == null) {
            return;
        }
        JobPriorityBlockingDeque queue = getQueue(taskTrackerNodeGroup);
        jobPo.setInternalExtParam(Constants.OLD_PRIORITY, String.valueOf(jobPo.getPriority()));

        jobPo.setPriority(Integer.MIN_VALUE);

        if (!queue.offer(jobPo)) {
            queue.pollLast(); // 移除优先级最低的一个
            queue.offer(jobPo);
        }
    }

    protected abstract JobPo getJob(String taskTrackerNodeGroup, String jobId);

    /**
     * 锁定任务
     */
    protected abstract boolean lockJob(String taskTrackerNodeGroup,
                                       String jobId,
                                       String taskTrackerIdentity,
                                       Long triggerTime,
                                       Long gmtModified);

    /**
     * 加载任务
     */
    protected abstract List<JobPo> load(String loadTaskTrackerNodeGroup, int loadSize);

    private JobPo get(String taskTrackerNodeGroup) {

        JobPriorityBlockingDeque queue = getQueue(taskTrackerNodeGroup);

        int size = queue.size();
        DotLogUtils.dot("AbstractPreLoader.queue size:{},taskTrackerNodeGroup:{}", size, taskTrackerNodeGroup);
        if (isInFactor(size)) {
            // 触发加载的请求
            if (!LOAD_SIGNAL.contains(taskTrackerNodeGroup)) {
                LOAD_SIGNAL.add(taskTrackerNodeGroup);
                doLoad();
            }
        }
        JobPo jobPo = queue.poll();
        if (jobPo != null && jobPo.getPriority() == Integer.MIN_VALUE) {
            if (CollectionUtils.isNotEmpty(jobPo.getInternalExtParams())) {
                if (jobPo.getInternalExtParams().containsKey(Constants.OLD_PRIORITY)) {
                    try {
                        int priority = Integer.parseInt(jobPo.getInternalExtParam(Constants.OLD_PRIORITY));
                        jobPo.getInternalExtParams().remove(Constants.OLD_PRIORITY);
                        jobPo.setPriority(priority);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return jobPo;
    }

    private boolean isInFactor(int size) {
        return size / (loadSize * 1.0) < factor;
    }

    private JobPriorityBlockingDeque getQueue(String taskTrackerNodeGroup) {
        JobPriorityBlockingDeque queue = JOB_MAP.get(taskTrackerNodeGroup);
        if (queue == null) {
            queue = new JobPriorityBlockingDeque(loadSize);
            JobPriorityBlockingDeque oldQueue = JOB_MAP.putIfAbsent(taskTrackerNodeGroup, queue);
            if (oldQueue != null) {
                queue = oldQueue;
            }
        }
        return queue;
    }
}
