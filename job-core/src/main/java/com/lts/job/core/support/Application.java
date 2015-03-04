package com.lts.job.core.support;

import com.lts.job.core.domain.JobNodeConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * 用来存储 程序的数据
 */
public class Application {

    private static final ConcurrentHashMap<String, Object> keyValue = new ConcurrentHashMap<String, Object>();

    public static void setAttribute(String key, Object value) {
        keyValue.put(key, value);
    }

    public static <T> T getAttribute(String key) {
        Object object = keyValue.get(key);
        if (object == null) {
            return null;
        }

        return (T) object;
    }
    public static final String KEY_AVAILABLE_THREADS = "availableThreads";

    public static final String JOB_RUNNING_CLASS = "JOB_RUNNING_CLASS";

    public static JobNodeConfig Config;

}
