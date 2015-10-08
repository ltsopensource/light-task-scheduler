package com.lts.queue;

import com.lts.core.Application;
import com.lts.core.cluster.Node;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.collect.ConcurrentHashSet;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.constant.EcTopic;
import com.lts.core.support.SystemClock;
import com.lts.ec.EventInfo;
import com.lts.ec.EventSubscriber;
import com.lts.ec.Observer;
import com.lts.queue.domain.JobPo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public abstract class AbstractPreLoader implements PreLoader {

    // 当前节点的序号
    private int curSequence = 0;
    private int totalNodes = 1;
    // 没个节点的步长
    protected int step = 500;
    // 预取阀值
    private double factor = 0.8;

    private ConcurrentHashMap<String/*taskTrackerNodeGroup*/, BlockingQueue<JobPo>> JOB_MAP = new ConcurrentHashMap<String, BlockingQueue<JobPo>>();

    // 加载的信号
    private ConcurrentHashSet<String> LOAD_SIGNAL = new ConcurrentHashSet<String>();
    private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);

    public AbstractPreLoader(final Application application) {
        if (start.compareAndSet(false, true)) {
            scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {

                    for (String loadTaskTrackerNodeGroup : LOAD_SIGNAL) {
                        BlockingQueue<JobPo> queue = JOB_MAP.get(loadTaskTrackerNodeGroup);
                        if (queue.size() / step < factor) {
                            // load
                            List<JobPo> loads = load(loadTaskTrackerNodeGroup, curSequence * step);
                            // 加入到内存中
                            if (CollectionUtils.isNotEmpty(loads)) {
                                queue.addAll(loads);
                            }
                        }
                        LOAD_SIGNAL.remove(loadTaskTrackerNodeGroup);
                    }
                }
            }, 500, 500, TimeUnit.MILLISECONDS);
        }

        application.getEventCenter().subscribe(new EventSubscriber(application.getConfig().getIdentity() + "_preLoader", new Observer() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                setCurSequence(application);
            }
        }), EcTopic.NODE_ADD, EcTopic.NODE_REMOVE);

        setCurSequence(application);
    }

    private void setCurSequence(Application application) {
        List<Node> nodes = application.getSubscribedNodeManager().getNodeList(NodeType.JOB_TRACKER);
        totalNodes = CollectionUtils.sizeOf(nodes);
        if (totalNodes == 0) {
            curSequence = 0;
        } else if (totalNodes == 1) {
            curSequence = 0;
        } else {
            List<Node> copy = new ArrayList<Node>(nodes);
            Collections.sort(copy, new Comparator<Node>() {
                @Override
                public int compare(Node left, Node right) {
                    return left.getCreateTime().compareTo(right.getCreateTime());
                }
            });

            int index = 0;
            for (Node node : copy) {
                if (node.getIdentity().equals(application.getConfig().getIdentity())) {
                    // 当前节点
                    curSequence = index;
                    break;
                }
                index++;
            }
        }
    }

    public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity) {
        while (true) {
            JobPo jobPo = get(taskTrackerNodeGroup);
            if (jobPo == null) {
                return null;
            }
            // update jobPo
            if (lockJob(taskTrackerNodeGroup, jobPo.getJobId(), taskTrackerIdentity, jobPo.getTriggerTime())) {
                jobPo.setTaskTrackerIdentity(taskTrackerIdentity);
                jobPo.setIsRunning(true);
                jobPo.setGmtModified(SystemClock.now());
                return jobPo;
            }
        }
    }

    /**
     * 锁定任务
     */
    protected abstract boolean lockJob(String taskTrackerNodeGroup, String jobId, String taskTrackerIdentity, Long triggerTime);

    /**
     * 加载任务
     */
    protected abstract List<JobPo> load(String loadTaskTrackerNodeGroup, int offset);

    private JobPo get(String taskTrackerNodeGroup) {
        BlockingQueue<JobPo> jobPos = JOB_MAP.get(taskTrackerNodeGroup);
        if (jobPos == null) {
            jobPos = new PriorityBlockingQueue<JobPo>(step, new Comparator<JobPo>() {
                @Override
                public int compare(JobPo left, JobPo right) {
                    int compare = left.getTriggerTime().compareTo(right.getTriggerTime());
                    if (compare != 0) {
                        return compare;
                    }
                    compare = left.getPriority().compareTo(left.getPriority());
                    if (compare != 0) {
                        return compare;
                    }
                    return left.getGmtCreated().compareTo(right.getGmtCreated());
                }
            });
            BlockingQueue<JobPo> oldJobPos = JOB_MAP.putIfAbsent(taskTrackerNodeGroup, jobPos);
            if (oldJobPos != null) {
                jobPos = oldJobPos;
            }
        }

        if (jobPos.size() / step < factor) {
            // 触发加载的请求
            if (!LOAD_SIGNAL.contains(taskTrackerNodeGroup)) {
                LOAD_SIGNAL.add(taskTrackerNodeGroup);
            }
        }
        return jobPos.poll();
    }

}
