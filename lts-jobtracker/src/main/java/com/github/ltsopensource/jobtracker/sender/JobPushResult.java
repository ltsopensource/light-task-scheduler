package com.github.ltsopensource.jobtracker.sender;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public enum JobPushResult {
    NO_JOB, // 没有任务可执行
    SUCCESS, //推送成功
    FAILED,      //推送失败
    SENT_ERROR
}
