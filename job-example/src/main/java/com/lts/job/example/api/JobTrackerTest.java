package com.lts.job.example.api;

import com.lts.job.example.support.MasterNodeChangeListenerImpl;
import com.lts.job.queue.mongo.MongoJobFeedbackQueue;
import com.lts.job.queue.mongo.MongoJobLogger;
import com.lts.job.queue.mongo.MongoJobQueue;
import com.lts.job.queue.mongo.store.Config;
import com.lts.job.tracker.JobTracker;
import com.lts.job.tracker.support.policy.OldDataDeletePolicy;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {

        final JobTracker jobTracker = new JobTracker();
        // 节点信息配置
        jobTracker.setZookeeperAddress("localhost:2181");
        jobTracker.setListenPort(35002); // 默认 35001
//        jobTracker.setClusterName("lts");

        jobTracker.addMasterNodeChangeListener(new MasterNodeChangeListenerImpl());

        // mongo 配置
        Config config = new Config();
        config.setAddresses(new String[]{"localhost:27017"});
        config.setUsername("lts");
        config.setPassword("lts");
        config.setDbName("job");
        jobTracker.setJobQueue(new MongoJobQueue(config));
        jobTracker.setJobFeedbackQueue(new MongoJobFeedbackQueue(config));
        jobTracker.setJobLogger(new MongoJobLogger(config));
        jobTracker.setOldDataHandler(new OldDataDeletePolicy());
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
