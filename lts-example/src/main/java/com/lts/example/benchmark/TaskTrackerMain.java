package com.lts.example.benchmark;

import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.example.support.NoopJobRunner;
import com.lts.tasktracker.TaskTracker;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
public class TaskTrackerMain {

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            startTaskTracker(i);
        }
    }

    private static void startTaskTracker(int index) {
        final TaskTracker taskTracker = new TaskTracker();
        // 任务执行类，实现JobRunner 接口
        taskTracker.setJobRunnerClass(NoopJobRunner.class);
        taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // taskTracker.setRegistryAddress("redis://127.0.0.1:6379");
        taskTracker.setNodeGroup("test_trade_TaskTracker_" + index); // 同一个TaskTracker集群这个名字相同
        taskTracker.setClusterName("test_cluster");
        taskTracker.setWorkThreads(10);
        // taskTracker.setDataPath(Constants.USER_HOME);
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        taskTracker.addMasterChangeListener(new MasterChangeListenerImpl());
        // 业务日志级别
        // taskTracker.setBizLoggerLevel(Level.INFO);
        // 可选址  leveldb(默认), rocksdb, berkeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        taskTracker.start();
    }

}
