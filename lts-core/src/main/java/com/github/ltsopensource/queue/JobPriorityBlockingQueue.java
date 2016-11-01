package com.github.ltsopensource.queue;

import com.github.ltsopensource.core.commons.concurrent.ConcurrentHashSet;
import com.github.ltsopensource.queue.domain.JobPo;

import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 主要做了一个去重的操作，当时同一个任务的时候，会覆盖
 *
 * @author Robert HG (254963746@qq.com) on 10/19/15.
 */
@SuppressWarnings("unchecked")
public class JobPriorityBlockingQueue {

    private final ReentrantLock lock;

    private transient Comparator<JobPo> comparator;

    private int capacity;

    private volatile int size;
    private JobPo[] queue;
    private ConcurrentHashSet<String/*jobId*/> JOB_ID_SET = new ConcurrentHashSet<String>();

    public JobPriorityBlockingQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        this.lock = new ReentrantLock();
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
        this.queue = new JobPo[this.capacity];
    }

    public int size() {
        return size;
    }

    public boolean offer(JobPo e) {
        if (e == null)
            throw new NullPointerException();
        if (size >= capacity) {
            // 满了，添加失败
            return false;
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n = size;
        try {
            if (JOB_ID_SET.contains(e.getJobId())) {
                // 如果已经存在了，替换
                replace(e);
            } else {
                siftUpUsingComparator(n, e, queue, comparator);
                size = n + 1;
                JOB_ID_SET.add(e.getJobId());
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public JobPo poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size - 1;
            if (n < 0)
                return null;
            else {
                JobPo[] array = queue;
                JobPo result = array[0];
                JobPo x = array[n];
                array[n] = null;
                siftDownUsingComparator(0, x, array, n, comparator);
                size = n;
                JOB_ID_SET.remove(result.getJobId());
                return result;
            }
        } finally {
            lock.unlock();
        }
    }

    private <E> void siftDownUsingComparator(int k, E x, Object[] array,
                                             int n,
                                             Comparator<? super E> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((E) c, (E) array[right]) > 0)
                    c = array[child = right];
                int code = cmp.compare(x, (E) c);
                if (code <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    private <E> void siftUpUsingComparator(int k, E x, Object[] array,
                                           Comparator<? super E> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            int compCode = cmp.compare(x, (E) e);
            if (compCode >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    private boolean replace(JobPo o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i != -1) {
                this.queue[i] = o;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private int indexOf(JobPo o) {
        if (o != null) {
            JobPo[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.getJobId().equals(array[i].getJobId()))
                    return i;
        }
        return -1;
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
        } finally {
            lock.unlock();
        }
    }

}
