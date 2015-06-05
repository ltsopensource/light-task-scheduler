package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;
import com.lts.job.task.tracker.expcetion.JobRunException;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * 任务执行者要实现的接口
 */
public interface JobRunner{

    /**
     * 执行任务
     *
     * @param job
     * @return
     * @throws JobRunException
     */
    public void run(Job job) throws Throwable;

}
