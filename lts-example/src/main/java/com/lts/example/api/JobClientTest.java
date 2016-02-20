package com.lts.example.api;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.core.exception.JobSubmitException;
import com.lts.core.spi.SpiKey;
import com.lts.example.support.BaseJobClientTest;
import com.lts.example.support.JobCompletedHandlerImpl;
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
@SuppressWarnings("rawtypes")
public class JobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws IOException {
//        submitWidthReplaceOnExist();
        console();
//        testProtector();
//        cancelJob();
    }

	public static void submitWidthReplaceOnExist() throws IOException {
        // 推荐使用RetryJobClient
        JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        jobClient.start();

        Job job = new Job();
        job.setTaskId("t_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression("0 0/1 * * * ?");
//        job.setTriggerTime(DateUtils.addDay(new Date(), 1));
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    public static void cancelJob(){
        JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//         jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
        // jobClient.setDataPath(Constants.USER_HOME);
        // 任务完成反馈接口
        jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        // 可选址  leveldb(默认), rocksdb, berkeleydb
        // taskTracker.addConfig("job.fail.store", "leveldb");
        jobClient.start();

        jobClient.cancelJob("t_4", "test_trade_TaskTracker");
    }


    public static void console() throws IOException {
        // 推荐使用RetryJobClient
        JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//         jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
        // jobClient.setDataPath(Constants.USER_HOME);
        // 任务完成反馈接口
        jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
        // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        // 可选址  leveldb(默认), rocksdb, berkeleydb, mapdb
        jobClient.addConfig("job.fail.store", "mapdb");
        jobClient.addConfig("lts.remoting.serializable.default", "hessian2");
//        jobClient.setIdentity("test_jobclient_0000001");
//        jobClient.addConfig(SpiKey.LTS_JSON, "ltsjson");
        jobClient.addConfig("lts.remoting", "netty");
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
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
//        jobClient.setDataPath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
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

        for (int i = 0; i < 10; i++) {

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