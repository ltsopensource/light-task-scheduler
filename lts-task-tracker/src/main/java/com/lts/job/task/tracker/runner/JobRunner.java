package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * 任务执行者要实现的接口
 */
public interface JobRunner{

    /**
     * 执行任务
     *
     * @param job
     */
    public void run(Job job) throws Throwable;

}
