package com.github.ltsopensource.example.api;

import com.github.ltsopensource.example.support.MasterChangeListenerImpl;
import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.jobtracker.JobTrackerBuilder;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {
        testMysqlQueue();
    }

    /**
     * 使用mysql做任务队列
     */
    public static void testMysqlQueue() {

        final JobTracker jobTracker = new JobTrackerBuilder()
                .setPropertiesConfigure("application.properties")
                .addMasterChangeListener(new MasterChangeListenerImpl())
                .build();

        jobTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                jobTracker.stop();
            }
        }));

    }

}