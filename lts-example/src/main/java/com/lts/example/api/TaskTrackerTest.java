package com.lts.example.api;

import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.example.support.NoopJobRunner;
import com.lts.example.support.TestJobRunner;
import com.lts.tasktracker.TaskTracker;


/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TaskTrackerTest {

    public static void main(String[] args) {

        final TaskTracker taskTracker = new TaskTracker();
        // 任务执行类，实现JobRunner 接口
        taskTracker.setJobRunnerClass(TestJobRunner.class);
        taskTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // taskTracker.setRegistryAddress("redis://127.0.0.1:6379");
        taskTracker.setNodeGroup("test_trade_TaskTracker"); // 同一个TaskTracker集群这个名字相同
        taskTracker.setClusterName("test_cluster");
        taskTracker.setWorkThreads(10);
        // 反馈任务给JobTracker失败，存储本地文件路径
        // taskTracker.setFailStorePath(Constants.USER_HOME);
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        taskTracker.addMasterChangeListener(new MasterChangeListenerImpl());
        // 业务日志级别
        // taskTracker.setBizLoggerLevel(Level.INFO);
        // 可选址  leveldb(默认), rocksdb, bekeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        taskTracker.addConfig("lts.monitor.url", "http://localhost:8080/");
        taskTracker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                taskTracker.stop();
            }
        }));
    }
}