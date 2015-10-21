package com.lts.queue;

import com.lts.queue.domain.JobPo;
import com.lts.queue.exception.DuplicateJobException;

/**
 * 定时任务队列
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public interface CronJobQueue extends JobQueue{

    /**
     * 添加任务
     *
     * @throws DuplicateJobException
     */
    boolean add(JobPo jobPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
    JobPo finish(String jobId);

    /**
     * 移除Cron Job
     */
    boolean remove(String jobId);

    /**
     * 得到JobPo
     */
    JobPo getJob(String taskTrackerNodeGroup, String taskId);

}
