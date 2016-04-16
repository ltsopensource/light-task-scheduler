package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public interface RepeatJobQueue extends SchedulerJobQueue {
    /**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(JobPo jobPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
    JobPo getJob(String jobId);

    /**
     * 移除Cron Job
     */
    boolean remove(String jobId);

    /**
     * 得到JobPo
     */
    JobPo getJob(String taskTrackerNodeGroup, String taskId);

    /**
     * 增加重复次数
     */
    int incRepeatedCount(String jobId);

}
