package com.lts.job.example.api;

import com.alibaba.fastjson.JSONObject;
import com.lts.job.client.JobClient;
import com.lts.job.client.RetryJobClient;
import com.lts.job.client.domain.Response;
import com.lts.job.core.domain.Job;
import com.lts.job.core.exception.JobSubmitException;
import com.lts.job.core.util.StringUtils;
import com.lts.job.example.support.BaseJobClientTest;
import com.lts.job.example.support.JobFinishedHandlerImpl;
import com.lts.job.example.support.MasterChangeListenerImpl;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws IOException {
        console();
//        testProtector();
    }

    public static void console() throws IOException {
        JobClient jobClient = new RetryJobClient();
//      final JobClient jobClient = new JobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobClient.setRegistryAddress("redis://127.0.0.1:6379");
        // 任务重试保存地址，默认用户目录下
//        jobClient.setFailStorePath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
//        jobClient.setLoadBalance("consistenthash");
        jobClient.addConfig("job.fail.store", "berkeleydb");
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
//        jobClient.setFailStorePath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        jobClient.addConfig("job.submit.concurrency.size", "3");
        jobClient.start();

        for (int i = 0; i < 50; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Job job = new Job();
                    job.setTaskId(StringUtils.generateUUID());
                    job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
                    job.setParam("shopId", "111");
                    try {
                        Response response = jobClient.submitJob(job);
                        System.out.println(JSONObject.toJSONString(response));
                    } catch (JobSubmitException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

}
