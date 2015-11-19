package com.lts.example.benchmark;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.example.support.JobCompletedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.example.support.MemoryStatus;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
@SuppressWarnings("rawtypes")
public class JobClientMain {

    public static void main(String[] args) {
        // 推荐使用RetryJobClient
        final JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 数据保存地址，默认用户目录下
        // jobClient.setData(Constants.USER_HOME);
        // 任务完成反馈接口
        jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        // 可选址  leveldb(默认), rocksdb, berkeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        jobClient.addConfig("job.submit.concurrency.size", "100");
        jobClient.start();

        try {
            // 休息1s 等待 连上JobTracker
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MemoryStatus.print();

        final AtomicLong num = new AtomicLong();

        final long start = System.currentTimeMillis();

        // 假设分了 100 个 partition
        final int partition = 100;

        final int totalSize = 1000000;

        final AtomicInteger thread = new AtomicInteger(100);
        for (int i = 0; i < thread.get(); i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Job job = new Job();
                        job.setTaskId(StringUtils.generateUUID());
                        job.setTaskTrackerNodeGroup("test_trade_TaskTracker_" + (num.get() % partition));
                        job.setParam("shopId", "111");
                        job.setNeedFeedback(false);

                        Response response = jobClient.submitJob(job);
                        if (!response.isSuccess()) {
                            System.out.println(response.getMsg());
                        } else {
                            if (num.incrementAndGet() > totalSize) {
                                if (thread.decrementAndGet() == 0) {
                                    MemoryStatus.print();
                                    System.out.println("totalSize : " + totalSize + " , time: " + (System.currentTimeMillis() - start) + "ms");
                                    break;
                                }
                            }
                        }
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(num.get());
                }
            }
        }).start();
    }

}
