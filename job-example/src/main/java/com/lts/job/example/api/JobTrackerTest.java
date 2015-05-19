package com.lts.job.example.api;

import com.lts.job.example.support.MasterChangeListenerImpl;
import com.lts.job.tracker.JobTracker;
import com.lts.job.tracker.support.policy.OldDataDeletePolicy;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {

        final JobTracker jobTracker = new JobTracker();
        // 节点信息配置
        jobTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobTracker.setRegistryAddress("redis://127.0.0.1:6379");
        jobTracker.setListenPort(35002); // 默认 35001
        jobTracker.setClusterName("test_cluster");

        jobTracker.addMasterChangeListener(new MasterChangeListenerImpl());

        // 设置业务日志记录
//        jobTracker.addConfig("job.logger", "mongo");
        // 任务队列用mongo
        jobTracker.addConfig("job.queue", "mongo");
        // mongo 配置
        jobTracker.addConfig("mongo.addresses", "127.0.0.1:27017");     // 多个地址用逗号分割
        jobTracker.addConfig("mongo.database", "job");

        jobTracker.setOldDataHandler(new OldDataDeletePolicy());
        // 设置 zk 客户端用哪个， 可选 zkclient, curator 默认是 zkclient
        jobTracker.addConfig("zk.client", "zkclient");
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
