package com.lts.job.core.domain;

/**
 * @author Robert HG (254963746@qq.com) on 8/28/14.
 */
public enum LogType {

    PUSH,             // 任务发送成功
    FINISHED,        // 任务执行完成
    RESEND,          // TaskTracker 重新发送的任务执行结果
    FIXED_DEAD       // 修复死掉的任务
    ;
}
