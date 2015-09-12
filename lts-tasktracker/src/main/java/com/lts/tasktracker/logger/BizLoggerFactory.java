package com.lts.tasktracker.logger;

import com.lts.core.cluster.LTSConfig;
import com.lts.core.constant.Environment;
import com.lts.core.constant.Level;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.tasktracker.domain.TaskTrackerApplication;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerFactory {

    private static final ConcurrentHashMap<String, BizLogger> BIZ_LOGGER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
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
                logger = create(level, remotingClient, application);

                BIZ_LOGGER_CONCURRENT_HASH_MAP.put(key, logger);
            }
        }
        return logger;
    }

    /**
     * 单元测试的时候返回 Mock
     */
    private static BizLogger create(Level level, RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        if (Environment.UNIT_TEST == LTSConfig.getEnvironment()) {
            return new MockBizLogger(level);
        }
        return new BizLoggerImpl(level, remotingClient, application);
    }

}
