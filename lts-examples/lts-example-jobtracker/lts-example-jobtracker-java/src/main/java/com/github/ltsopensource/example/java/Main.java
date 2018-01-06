package com.github.ltsopensource.example.java;

import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.jobtracker.JobTrackerBuilder;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class Main {

    public static void main(String[] args) {
//        final JobTracker jobTracker = new JobTracker();
//        // 节点信息配置
//        jobTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
////        jobTracker.setRegistryAddress("redis://127.0.0.1:6379");
//        jobTracker.setListenPort(35001); // 默认 35001
//        jobTracker.setClusterName("test_cluster");
//
////        // 设置业务日志记录 mysql
////        jobTracker.addConfig("job.logger", "mysql");
////        // 任务队列用mysql
////        jobTracker.addConfig("job.queue", "mysql");
//        // mysql 配置
//        jobTracker.addConfig("jdbc.url", "jdbc:mysql://127.0.0.1:3306/lts");
//        jobTracker.addConfig("jdbc.username", "root");
//        jobTracker.addConfig("jdbc.password", "root");

        final JobTracker jobTracker = new JobTrackerBuilder()
                .setPropertiesConfigure("lts.properties")
                .build();

        // 启动节点
        jobTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobTracker.stop();
            }
        }));
    }

}
