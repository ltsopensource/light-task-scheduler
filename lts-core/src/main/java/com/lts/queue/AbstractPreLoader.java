package com.lts.queue;

import com.lts.core.cluster.Config;
import com.lts.core.commons.concurrent.ConcurrentHashSet;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.support.SystemClock;
import com.lts.queue.domain.JobPo;

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

    private ConcurrentHashMap<String/*taskTrackerNodeGroup*/, JobPriorityBlockingQueue> JOB_MAP = new ConcurrentHashMap<String, JobPriorityBlockingQueue>();

    // 加载的信号
    private ConcurrentHashSet<String> LOAD_SIGNAL = new ConcurrentHashSet<String>();
    private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-PreLoader", true));
    @SuppressWarnings("unused")
	private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);
    private String FORCE_PREFIX = "force_"; // 强制加载的信号

    public AbstractPreLoader(final Config config) {
        if (start.compareAndSet(false, true)) {

            loadSize = config.getParameter("job.preloader.size", 300);
            factor = config.getParameter("job.preloader.factor", 0.2);

            scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {

                    for (String loadTaskTrackerNodeGroup : LOAD_SIGNAL) {

                        // 是否是强制加载
                        boolean force = false;
                        if (loadTaskTrackerNodeGroup.startsWith(FORCE_PREFIX)) {
                            loadTaskTrackerNodeGroup = loadTaskTrackerNodeGroup.replaceFirst(FORCE_PREFIX, "");
                            force = true;
                        }

                        JobPriorityBlockingQueue queue = JOB_MAP.get(loadTaskTrackerNodeGroup);
                        if (!force && queue.size() / loadSize < factor) {
                            // load
                            List<JobPo> loads = load(loadTaskTrackerNodeGroup, loadSize - queue.size());
                            // 加入到内存中
                            if (CollectionUtils.isNotEmpty(loads)) {
                                for (JobPo load : loads) {
                                    if (!queue.offer(load)) {
                                        // 没有成功说明已经满了
                                        break;
                                    }
                                }
                            }
                        }
                        LOAD_SIGNAL.remove(loadTaskTrackerNodeGroup);
                    }
                }
            }, 500, 500, TimeUnit.MILLISECONDS);
        }
    }

    public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity) {
        while (true) {
            JobPo jobPo = get(taskTrackerNodeGroup);
            if (jobPo == null) {
                return null;
            }
            // update jobPo
            if (lockJob(taskTrackerNodeGroup, jobPo.getJobId(),
                    taskTrackerIdentity, jobPo.getTriggerTime(),
                    jobPo.getGmtModified())) {
                jobPo.setTaskTrackerIdentity(taskTrackerIdentity);
                jobPo.setIsRunning(true);
                jobPo.setGmtModified(SystemClock.now());
                return jobPo;
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
        JobPriorityBlockingQueue queue = JOB_MAP.get(taskTrackerNodeGroup);
        if (queue == null) {
            queue = new JobPriorityBlockingQueue(loadSize);
            JobPriorityBlockingQueue oldQueue = JOB_MAP.putIfAbsent(taskTrackerNodeGroup, queue);
            if (oldQueue != null) {
                queue = oldQueue;
            }
        }

        if (queue.size() / loadSize < factor) {
            // 触发加载的请求
            if (!LOAD_SIGNAL.contains(taskTrackerNodeGroup)) {
                LOAD_SIGNAL.add(taskTrackerNodeGroup);
            }
        }
        return queue.poll();
    }

}
