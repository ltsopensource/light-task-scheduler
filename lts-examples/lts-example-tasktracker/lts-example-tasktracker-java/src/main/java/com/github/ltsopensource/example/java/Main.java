package com.github.ltsopensource.example.java;

import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.TaskTrackerBuilder;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class Main {

    public static void main(String[] args) {

//        final TaskTracker taskTracker = new TaskTracker();
//        // 任务执行类，实现JobRunner 接口
//        taskTracker.setJobRunnerClass(TestJobRunner.class);
//        taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        taskTracker.setNodeGroup("test_trade_TaskTracker"); // 同一个TaskTracker集群这个名字相同
//        taskTracker.setClusterName("test_cluster");
//        taskTracker.setWorkThreads(64);
//        taskTracker.addConfig("job.fail.store", "mapdb");

        final TaskTracker taskTracker = new TaskTrackerBuilder()
                .setPropertiesConfigure("lts.properties")
                .build();

        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));
    }
}
