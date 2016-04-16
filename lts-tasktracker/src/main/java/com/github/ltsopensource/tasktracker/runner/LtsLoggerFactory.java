package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.tasktracker.logger.BizLogger;

/**
 * 这个日志器将日志发送给LTS平台
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public final class LtsLoggerFactory {

    private static final ThreadLocal<BizLogger> THREAD_LOCAL = new ThreadLocal<BizLogger>();

    public static BizLogger getBizLogger() {
        return THREAD_LOCAL.get();
    }

    protected static void setLogger(BizLogger logger){
        THREAD_LOCAL.set(logger);
    }

    protected static void remove(){
        THREAD_LOCAL.remove();
    }
}
