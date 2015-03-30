package com.lts.job.task.tracker.logger;

import com.lts.job.core.constant.Level;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.task.tracker.domain.TaskTrackerApplication;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerFactory {

    private static ConcurrentHashMap<String, BizLogger> map = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
     *
     * @param level
     * @param remotingClient
     */
    public static BizLogger getLogger(Level level, RemotingClientDelegate remotingClient, TaskTrackerApplication application) {
        String key = application.getConfig().getIdentity();
        BizLogger logger = map.get(key);
        if (logger == null) {
            synchronized (map) {
                logger = map.get(key);
                if (logger != null) {
                    return logger;
                }
                logger = new BizLoggerImpl(
                        level,
                        remotingClient, application);
                map.put(key, logger);
            }
        }
        return logger;
    }

}
