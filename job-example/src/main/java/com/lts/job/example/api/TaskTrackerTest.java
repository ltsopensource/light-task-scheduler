package com.lts.job.example.api;

import com.lts.job.example.support.MasterChangeListenerImpl;
import com.lts.job.example.support.TestJobRunner;
import com.lts.job.task.tracker.TaskTracker;


/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {
        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.setJobRunnerClass(TestJobRunner.class);
        taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        taskTracker.setRegistryAddress("redis://127.0.0.1:6379");
        taskTracker.setNodeGroup("test_trade_TaskTracker");
        taskTracker.setClusterName("test_cluster");
        taskTracker.setWorkThreads(20);
//        taskTracker.setJobInfoSavePath(Constants.USER_HOME);
        taskTracker.addMasterChangeListener(new MasterChangeListenerImpl());
//        taskTracker.setBizLoggerLevel(Level.INFO);        // 业务日志级别
        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));

    }
}