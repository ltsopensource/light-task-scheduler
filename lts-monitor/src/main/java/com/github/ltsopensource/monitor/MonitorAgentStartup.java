package com.github.ltsopensource.monitor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 3/5/16.
 */
public class MonitorAgentStartup {

    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }

    public static void start(String cfgPath) {
    }

    public static void stop() {
    }

}
