package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.tasktracker.Result;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行者要实现的接口
 */
public interface JobRunner {

    /**
     * 执行任务
     * 抛出异常则消费失败, 返回null则认为是消费成功
     */
    Result run(JobContext jobContext) throws Throwable;

}
