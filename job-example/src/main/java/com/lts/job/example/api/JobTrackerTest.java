package com.lts.job.example.api;

import com.lts.job.store.Config;
import com.lts.job.tracker.JobTracker;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {

        final JobTracker jobTracker = new JobTracker();
        // 节点信息配置
        jobTracker.setZookeeperAddress("localhost:2181");
//        jobTracker.setListenPort(35001); // 默认 35001
//        jobTracker.setClusterName("lts");

        // mongo 配置 （也可以配置在 mongo.properties中）
        Config config = new Config();
        config.setAddresses(new String[]{"localhost:27017"});
        config.setUsername("lts");
        config.setPassword("lts");
        config.setDbName("job");
        jobTracker.setStoreConfig(config);

        // 启动节点
        jobTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobTracker.stop();
            }
        }));

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
