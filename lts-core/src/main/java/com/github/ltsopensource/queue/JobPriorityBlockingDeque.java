package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 优先级 有界 去重 双向队列
 * @author Robert HG (254963746@qq.com) on 8/5/16.
 */
public class JobPriorityBlockingDeque {

    private final int capacity;

    private final LinkedList<JobPo> list;
    private final ReentrantLock lock = new ReentrantLock();

    // Key: jobId     value:gmtModified
    private Map<String, Long> jobs = new ConcurrentHashMap<String, Long>();

    private Comparator<JobPo> comparator;

    public JobPriorityBlockingDeque(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        this.list = new LinkedList<JobPo>();
        this.comparator = new Comparator<JobPo>() {
            @Override
            public int compare(JobPo left, JobPo right) {
                if (left.getJobId().equals(right.getJobId())) {
                    return 0;
                }
                int compare = left.getPriority().compareTo(right.getPriority());
                if (compare != 0) {
                    return compare;
                }
                compare = left.getTriggerTime().compareTo(right.getTriggerTime());
                if (compare != 0) {
                    return compare;
                }
                compare = left.getGmtCreated().compareTo(right.getGmtCreated());
                if (compare != 0) {
                    return compare;
                }
                return -1;
            }
        };
    }

    public JobPo pollFirst() {
        lock.lock();
        try {
            JobPo f = list.pollFirst();
            if (f == null)
                return null;
            jobs.remove(f.getJobId());
            return f;
        } finally {
            lock.unlock();
        }
    }

    public JobPo pollLast() {
        lock.lock();
        try {
            JobPo l = list.pollLast();
            if (l == null)
                return null;
            jobs.remove(l.getJobId());
            return l;
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(JobPo e) {
        if (e == null) throw new NullPointerException();
        if (list.size() >= capacity)
            return false;

        lock.lock();
        try {
            if (jobs.containsKey(e.getJobId())) {
                // 如果已经在内存中了，check下是否和内存中的一致
                Long gmtModified = jobs.get(e.getJobId());
                if (gmtModified != null && !gmtModified.equals(e.getGmtModified())) {
                    // 删除原来的
                    removeOld(e);
                }
            }

            int insertionPoint = Collections.binarySearch(list, e, comparator);
            if (insertionPoint < 0) {
                // this means the key didn't exist, so the insertion point is negative minus 1.
                insertionPoint = -insertionPoint - 1;
            }

            list.add(insertionPoint, e);
            jobs.put(e.getJobId(), e.getGmtModified());
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void removeOld(JobPo e) {
        Iterator<JobPo> i = iterator();
        int index = 0;
        while (i.hasNext()) {
            JobPo o = i.next();
            if (o.getJobId().equals(e.getJobId())) {
                list.remove(index);
                jobs.remove(e.getJobId());
                return;
            }
            index++;
        }
    }

    public JobPo poll() {
        return pollFirst();
    }

    public int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            Iterator<JobPo> i = iterator();
            if (!i.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (; ; ) {
                JobPo e = i.next();
                sb.append(e);
                if (!i.hasNext())
                    return sb.append(']').toString();
                sb.append(", ");
            }
        } finally {
            lock.unlock();
        }
    }

    public Iterator<JobPo> iterator() {
        return list.iterator();
    }

}
