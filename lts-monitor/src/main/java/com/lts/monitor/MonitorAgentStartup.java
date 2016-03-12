package com.lts.monitor;

import com.lts.core.commons.utils.StringUtils;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 3/5/16.
 */
public class MonitorAgentStartup {

    public static void main(String[] args) {
        String cfgPath = args[0];
        start(cfgPath);
    }

    private static void start(String cfgPath) {

        try {
            MonitorCfg cfg = MonitorCfgLoader.load(cfgPath);

            final MonitorAgent agent = new MonitorAgent();

            agent.setRegistryAddress(cfg.getRegistryAddress());
            agent.setClusterName(cfg.getClusterName());
            if (StringUtils.isNotEmpty(cfg.getBindIp())) {
                agent.setBindIp(cfg.getBindIp());
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

}
