package com.github.ltsopensource.tasktracker.runner;

/**
 * 实现这个类可以自定义在中断时候的操作
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public interface InterruptibleJobRunner extends JobRunner {

    /**
     * 当任务被cancel(中断)的时候,调用这个
     */
    void interrupt();
}
