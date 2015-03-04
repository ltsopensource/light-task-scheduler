package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 * 默认的 任务 处理器, 不执行什么
 */
public class DefaultJobRunner implements JobRunner {

    @Override
    public void run(Job job) throws Throwable {
        // do nothing
    }
}
