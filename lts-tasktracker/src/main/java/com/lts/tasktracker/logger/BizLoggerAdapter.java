package com.lts.tasktracker.logger;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public abstract class BizLoggerAdapter implements BizLogger {

    public abstract void setId(String jobId, String taskId);

    public abstract void removeId();

}
