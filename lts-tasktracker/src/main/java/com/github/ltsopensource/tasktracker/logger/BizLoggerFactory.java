package com.github.ltsopensource.tasktracker.logger;

import com.github.ltsopensource.core.cluster.LTSConfig;
import com.github.ltsopensource.core.constant.Environment;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.core.remoting.RemotingClientDelegate;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerFactory {

    private static final ConcurrentHashMap<String, BizLogger> BIZ_LOGGER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
     */
    public static BizLogger getLogger(Level level, RemotingClientDelegate remotingClient, TaskTrackerAppContext appContext) {

        // 单元测试的时候返回 Mock
        if (Environment.UNIT_TEST == LTSConfig.getEnvironment()) {
            return new MockBizLogger(level);
        }

        String key = appContext.getConfig().getIdentity();
        BizLogger logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
        if (logger == null) {
            synchronized (BIZ_LOGGER_CONCURRENT_HASH_MAP) {
                logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
                if (logger != null) {
                    return logger;
                }
                logger = new BizLoggerImpl(level, remotingClient, appContext);

                BIZ_LOGGER_CONCURRENT_HASH_MAP.put(key, logger);
            }
        }
        return logger;
    }

}
