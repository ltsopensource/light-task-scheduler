package com.github.ltsopensource.core.commons.utils;

/**
 * @author Robert HG (254963746@qq.com) on 11/1/16.
 */
public class SystemPropertyUtils {

    private static boolean enableDotLog;
    private static boolean enablePeriod = false;

    static {
        enableDotLog = "true".equals(System.getProperty("enableDotLog"));
        enablePeriod = "true".equals(System.getProperty("enablePeriod"));
    }

    public static boolean isEnableDotLog() {
        return enableDotLog;
    }

    public static boolean isEnablePeriod() {
        return enablePeriod;
    }
}
