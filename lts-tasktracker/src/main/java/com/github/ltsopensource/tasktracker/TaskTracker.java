package com.github.ltsopensource.tasktracker;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14. 任务执行节点
 */
public interface TaskTracker {

    void start();

    void stop();

    void beforeStart();

    void afterStart();

    void afterStop();

    void beforeStop();
}
