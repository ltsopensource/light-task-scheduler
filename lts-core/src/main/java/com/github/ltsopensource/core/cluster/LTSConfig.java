package com.github.ltsopensource.core.cluster;

import com.github.ltsopensource.core.constant.Environment;

/**
 * 全局变量
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
public class LTSConfig {

    private static Environment environment = Environment.ONLINE;

    public static Environment getEnvironment() {
        if (environment == null) {
            return Environment.ONLINE;
        }
        return environment;
    }

    public static void setEnvironment(Environment environment) {
        LTSConfig.environment = environment;
    }
}
