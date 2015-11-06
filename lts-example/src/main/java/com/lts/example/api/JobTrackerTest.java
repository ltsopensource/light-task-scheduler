package com.lts.example.api;

import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobtracker.JobTracker;
import com.lts.jobtracker.support.policy.OldDataDeletePolicy;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobTrackerTest {

    public static void main(String[] args) {

        // 1. 使用mongo做任务队列
//        testMongoQueue();
//         2. 使用mysql做任务队列
        testMysqlQueue();
    }

    /**
     * 使用mongo做任务队列
     */
    public static void testMongoQueue() {
        final JobTracker jobTracker = new JobTracker();
        // 节点信息配置
        jobTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // jobTracker.setRegistryAddress("redis://127.0.0.1:6379");
        // jobTracker.setListenPort(35002); // 默认 35001
        jobTracker.setClusterName("test_cluster");   // 三种节点都要保持一致

        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobTracker.addMasterChangeListener(new MasterChangeListenerImpl());

        // 设置业务日志记录， 可选值: mongo, mysql , console
        jobTracker.addConfig("job.logger", "mongo");
        // 任务队列用mongo
        jobTracker.addConfig("job.queue", "mongo");
        // mongo 配置
        jobTracker.addConfig("mongo.addresses", "127.0.0.1:27017");     // 多个地址用逗号分割
        jobTracker.addConfig("mongo.database", "lts");

        // 这个是对于 返回给客户端 任务的 老数据删除策略
        jobTracker.setOldDataHandler(new OldDataDeletePolicy());
        // 设置 zk 客户端用哪个， 可选 zkclient(默认), curator
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

    /**
     * 使用mysql做任务队列
     */
    public static void testMysqlQueue() {
        final JobTracker jobTracker = new JobTracker();
        // 节点信息配置
        jobTracker.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobTracker.setRegistryAddress("redis://127.0.0.1:6379");
        jobTracker.setListenPort(35002); // 默认 35001
        jobTracker.setClusterName("test_cluster");

        jobTracker.addMasterChangeListener(new MasterChangeListenerImpl());

        // 设置业务日志记录 mysql
        jobTracker.addConfig("job.logger", "mysql");
        // 任务队列用mysql
        jobTracker.addConfig("job.queue", "mysql");
        // mysql 配置
        jobTracker.addConfig("jdbc.url", "jdbc:mysql://127.0.0.1:3306/lts");
        jobTracker.addConfig("jdbc.username", "root");
        jobTracker.addConfig("jdbc.password", "root");
        // 可选值 mina netty
//         jobTracker.addConfig("lts.remoting", "mina");
        // 可选值 fastjson hessian2 java
//         jobTracker.addConfig("lts.remoting.serializable.default", "fastjson");
        // 延迟批量刷盘业务日志开关
//        jobTracker.addConfig("lazy.job.logger", "true");

        jobTracker.setOldDataHandler(new OldDataDeletePolicy());
        // 设置 zk 客户端用哪个， 可选 zkclient, curator 默认是 zkclient
//        jobTracker.addConfig("zk.client", "curator");

        jobTracker.addConfig("lts.monitor.url", "http://localhost:8081/");

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
