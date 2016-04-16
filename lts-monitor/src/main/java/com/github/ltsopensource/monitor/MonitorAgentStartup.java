package com.github.ltsopensource.monitor;

import com.github.ltsopensource.core.commons.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 3/5/16.
 */
public class MonitorAgentStartup {

    private final static MonitorAgent agent = new MonitorAgent();
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }

    public static void start(String cfgPath) {

        if (!started.compareAndSet(false, true)) {
            return;
        }

        try {
            MonitorCfg cfg = MonitorCfgLoader.load(cfgPath);

            agent.setRegistryAddress(cfg.getRegistryAddress());
            agent.setClusterName(cfg.getClusterName());
            if (StringUtils.isNotEmpty(cfg.getBindIp())) {
                agent.setBindIp(cfg.getBindIp());
            }
            if (StringUtils.isNotEmpty(cfg.getIdentity())) {
                agent.setIdentity(cfg.getIdentity());
            }
            for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
                agent.addConfig(config.getKey(), config.getValue());
            }

            agent.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    agent.stop();
                }
            }));

        } catch (CfgException e) {
            System.err.println("Monitor Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (started.compareAndSet(true, false)) {
            agent.stop();
        }
    }

}
