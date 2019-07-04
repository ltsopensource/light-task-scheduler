package com.github.ltsopensource.jobtracker.support;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         任务分发管理
 */
public interface JobPusher {

    void push(final String request);
}
