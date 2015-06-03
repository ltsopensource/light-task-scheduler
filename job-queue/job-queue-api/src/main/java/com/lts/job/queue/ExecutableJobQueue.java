package com.lts.job.queue;

import com.lts.job.queue.domain.JobPo;

import java.util.List;

/**
 * 等待执行的任务队列 (可以有多个)
 *
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public interface ExecutableJobQueue {

    /**
     * 创建一个队列
     */
    boolean createQueue(String taskTrackerNodeGroup);

    /**
     * 入队列
     */
    boolean add(JobPo jobPo);

    /**
     * 从队列中取一个元素，并锁住这个元素
     */
    JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity);

    /**
     * 出队列
     */
    boolean remove(String taskTrackerNodeGroup, String jobId);

    /**
     * reset , runnable
     */
    void resume(JobPo jobPo);

    /**
     * 得到死任务
     *
     * @param taskTrackerNodeGroup
     * @param deadline
     * @return
     */
    List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline);
}
