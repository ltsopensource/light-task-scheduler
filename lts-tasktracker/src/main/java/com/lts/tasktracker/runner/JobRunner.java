package com.lts.tasktracker.runner;

import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行者要实现的接口
 */
public interface JobRunner {

    /**
     * 执行任务
     * 抛出异常则消费失败, 返回null则认为是消费成功
     */
    public Result run(Job job) throws Throwable;

}
