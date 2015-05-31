package com.lts.job.task.tracker.logger;

import com.lts.job.core.constant.Level;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerFactory {

    private static final ConcurrentHashMap<String, BizLogger> BIZ_LOGGER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
     *
     * @param level
     * @param remotingClient
     */
    public static BizLogger getLogger(Level level, RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        String key = application.getConfig().getIdentity();
        BizLogger logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
        if (logger == null) {
            synchronized (BIZ_LOGGER_CONCURRENT_HASH_MAP) {
                logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
                if (logger != null) {
                    return logger;
                }
                logger = new BizLoggerImpl(
                        level,
                        remotingClient, application);
                BIZ_LOGGER_CONCURRENT_HASH_MAP.put(key, logger);
            }
        }
        return logger;
    }

}
