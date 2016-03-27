package com.lts.core.domain;

import com.lts.core.commons.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJob extends Job {

    /**
     * 依赖的任务
     */
    private List<Job> depJobs;

    private ReentrantLock lock = new ReentrantLock();

    public void addDepJob(Job job) {
        lock.lock();
        try {
            if (depJobs == null) {
                depJobs = new ArrayList<Job>();
                depJobs.add(job);
            } else {
                for (Job depJob : depJobs) {
                    if (depJob.getTaskId().equals(job.getTaskId())
                            && depJob.getTaskTrackerNodeGroup().equals(job.getTaskTrackerNodeGroup())) {
                        // 是否已经添加进去
                        return;
                    }
                }
                depJobs.add(job);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addDepJobs(List<Job> jobs) {
        if (CollectionUtils.isNotEmpty(jobs)) {
            for (Job job : jobs) {
                addDepJob(job);
            }
        }
    }

    public void removeDepJob(Job job) {
        lock.lock();

        try {
            if (CollectionUtils.isEmpty(depJobs)) {
                return;
            }
            Job removeJob = null;
            for (Job depJob : depJobs) {
                if (depJob.getTaskId().equals(job.getTaskId())
                        && depJob.getTaskTrackerNodeGroup().equals(job.getTaskTrackerNodeGroup())) {
                    removeJob = depJob;
                    break;
                }
            }
            if (removeJob != null) {
                depJobs.remove(removeJob);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Job> getDepJobs() {
        return depJobs;
    }

    public void setDepJobs(List<Job> depJobs) {
        this.depJobs = depJobs;
    }
}
