package com.github.ltsopensource.jobtracker.support;

/**
 * @author Robert HG (254963746@qq.com) on 8/1/14. 任务处理器
 */
public interface JobReceiver {

    /**
     * jobTracker 接受任务
     */
    public void receive(String request);
}
