package com.lts.example.benchmark;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.example.support.JobFinishedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
public class JobClientMain {

    public static void main(String[] args) {
        // 推荐使用RetryJobClient
        final JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
        // jobClient.setFailStorePath(Constants.USER_HOME);
        // 任务完成反馈接口
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        // 可选址  leveldb(默认), rocksdb, bekeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        jobClient.addConfig("job.submit.concurrency.size", "100");
        jobClient.start();

        try {
            // 休息1s 等待 连上JobTracker
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final AtomicLong num = new AtomicLong();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // 假设分了 20 个 partition

        final int partition = 100;

        for (int i = 0; i < 100; i++) {

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
                            num.incrementAndGet();
                        }
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
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
