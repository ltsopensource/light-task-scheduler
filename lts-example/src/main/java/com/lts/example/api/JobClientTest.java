package com.lts.example.api;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;
import com.lts.example.support.BaseJobClientTest;
import com.lts.example.support.JobFinishedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws IOException {
        console();
//        testProtector();
    }

    public static void console() throws IOException {
        // 推荐使用RetryJobClient
        JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//         jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
        // jobClient.setFailStorePath(Constants.USER_HOME);
        // 任务完成反馈接口
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        // 可选址  leveldb(默认), rocksdb, berkeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        jobClient.start();

        JobClientTest jobClientTest = new JobClientTest();
        jobClientTest.jobClient = jobClient;
        jobClientTest.startConsole();
    }

    public static void testProtector() {

        final JobClient jobClient = new RetryJobClient();
//      final JobClient jobClient = new JobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
//        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
//        jobClient.setFailStorePath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
//                jobClient.addConfig("job.fail.store", "leveldb");     // 默认
//        jobClient.addConfig("job.fail.store", "berkeleydb");
//        jobClient.addConfig("job.fail.store", "rocksdb");
        jobClient.start();

        fastSubmit(jobClient);
    }

    private static void fastSubmit(final JobClient jobClient) {
        final AtomicLong num = new AtomicLong();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        for (int i = 0; i < 50; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Job job = new Job();
                        job.setTaskId(StringUtils.generateUUID());
                        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
                        job.setParam("shopId", "111");
                        job.setNeedFeedback(false);
                        try {
                            Response response = jobClient.submitJob(job);
                            System.out.print(" " + num.incrementAndGet());
                            if (num.incrementAndGet() % 50 == 0) {
                                System.out.println("");
                            }
//                            System.out.println(JSONObject.toJSONString(response));
                        } catch (JobSubmitException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

}