package com.lts.example.api;

import com.lts.example.support.TestJobRunner;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.tasktracker.TaskTracker;

/**
 * @author Robert HG (254963746@qq.com) on 3/14/16.
 */
public class MultiNodeTest {

    public static void main(String[] args) throws InterruptedException {
        final TaskTracker taskTracker = new TaskTracker();
        // 任务执行类，实现JobRunner 接口
        taskTracker.setJobRunnerClass(TestJobRunner.class);
        taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
        taskTracker.setNodeGroup("test_trade_TaskTracker"); // 同一个TaskTracker集群这个名字相同
        taskTracker.setClusterName("test_cluster");
        taskTracker.setWorkThreads(10);
        taskTracker.start();

        final TaskTracker taskTracker2 = new TaskTracker();
        // 任务执行类，实现JobRunner 接口
        taskTracker2.setJobRunnerClass(TestJobRunner.class);
        taskTracker2.setRegistryAddress("zookeeper://127.0.0.1:2181");
        taskTracker2.setNodeGroup("test_trade_TaskTracker"); // 同一个TaskTracker集群这个名字相同
        taskTracker2.setClusterName("test_cluster");
        taskTracker2.setWorkThreads(10);
        taskTracker2.start();

//        JobClient jobClient = new RetryJobClient();
//        jobClient.setNodeGroup("test_jobClient");
//        jobClient.setClusterName("test_cluster");
//        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobClient.start();

        Thread.sleep(3000L);
        taskTracker.stop();

//        Thread.sleep(3000L);
//        jobClient.stop();

        Thread.sleep(100000000000000L);
    }

}
