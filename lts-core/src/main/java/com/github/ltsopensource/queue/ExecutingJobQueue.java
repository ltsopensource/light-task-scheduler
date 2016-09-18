package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;

import java.util.List;

/**
 * 正在执行的 任务队列
 *
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public interface ExecutingJobQueue extends JobQueue {

    /**
     * 入队列
     */
    boolean add(JobPo jobPo);

    /**
     * 出队列
     */
    boolean remove(String jobId);

    /**
     * 得到某个TaskTracker节点上正在执行的任务
     */
    List<JobPo> getJobs(String taskTrackerIdentity);

    /**
     * 根据过期时间得到死掉的任务
     */
    List<JobPo> getDeadJobs(long deadline);

    /**
     * 得到JobPo
     */
    JobPo getJob(String taskTrackerNodeGroup, String taskId);

    JobPo getJob(String jobId);
}
