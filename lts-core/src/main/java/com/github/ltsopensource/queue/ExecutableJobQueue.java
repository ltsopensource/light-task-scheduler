package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;

import java.util.List;

/**
 * 等待执行的任务队列 (可以有多个)
 *
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public interface ExecutableJobQueue extends JobQueue {

    /**
     * 创建一个队列
     */
    boolean createQueue(String taskTrackerNodeGroup);

    /**
     * 删除
     */
    boolean removeQueue(String taskTrackerNodeGroup);

    /**
     * 入队列
     */
    boolean add(JobPo jobPo);

    /**
     * 出队列
     */
    boolean remove(String taskTrackerNodeGroup, String jobId);

    long countJob(String realTaskId, String taskTrackerNodeGroup);

    boolean removeBatch(String realTaskId, String taskTrackerNodeGroup);

    /**
     * reset , runnable
     */
    void resume(JobPo jobPo);

    /**
     * 得到死任务
     */
    List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline);

    /**
     * 得到JobPo
     */
    JobPo getJob(String taskTrackerNodeGroup, String taskId);
}
