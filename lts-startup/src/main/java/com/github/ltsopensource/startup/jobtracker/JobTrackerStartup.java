package com.github.ltsopensource.startup.jobtracker;

import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.jobtracker.support.policy.OldDataDeletePolicy;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JobTrackerStartup {

    public static void main(String[] args) {
        try {

            String confPath = args[0];

            JobTrackerCfg cfg = JobTrackerCfgLoader.load(confPath);

            final JobTracker jobTracker = new JobTracker();
            jobTracker.setRegistryAddress(cfg.getRegistryAddress());
            jobTracker.setListenPort(cfg.getListenPort());
            jobTracker.setClusterName(cfg.getClusterName());
            if (StringUtils.isNotEmpty(cfg.getBindIp())) {
                jobTracker.setBindIp(cfg.getBindIp());
            }

            jobTracker.setOldDataHandler(new OldDataDeletePolicy());

            for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
                jobTracker.addConfig(config.getKey(), config.getValue());
            }
            // 启动节点
            jobTracker.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    jobTracker.stop();
                }
            }));

        } catch (CfgException e) {
            System.err.println("JobTracker Startup Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
