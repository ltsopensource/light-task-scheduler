package com.github.ltsopensource.queue;

import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.store.jdbc.exception.DupEntryException;

/**
 * 定时任务队列
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public interface CronJobQueue extends SchedulerJobQueue {

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

}
