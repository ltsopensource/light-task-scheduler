package com.lts.startup;

import com.lts.jobtracker.JobTracker;
import com.lts.jobtracker.support.policy.OldDataDeletePolicy;

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
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
