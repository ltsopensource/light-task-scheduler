package com.lts.job.task.tracker.runner;

import com.lts.job.task.tracker.logger.BizLogger;

/**
 * 这个日志器将日志发送给LTS平台
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public final class LtsLogger {

    private static ThreadLocal<BizLogger> threadLocal = new ThreadLocal<BizLogger>();

    public static BizLogger getBizLogger() {
        return threadLocal.get();
    }

    protected static void setLogger(BizLogger logger){
        threadLocal.set(logger);
    }

    protected static void remove(){
        threadLocal.remove();
    }
}
