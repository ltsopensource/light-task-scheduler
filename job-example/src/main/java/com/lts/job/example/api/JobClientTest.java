package com.lts.job.example.api;

import com.lts.job.client.JobClient;
import com.lts.job.client.RetryJobClient;
import com.lts.job.example.support.BaseJobClientTest;
import com.lts.job.example.support.JobFinishedHandlerImpl;
import com.lts.job.example.support.MasterChangeListenerImpl;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
public class JobClientTest extends BaseJobClientTest {

    public static void main(String[] args) throws IOException {

        JobClient jobClient = new RetryJobClient();
//      final JobClient jobClient = new JobClient();
        jobClient.setNodeGroup("test_jobClient");
//        jobClient.setClusterName("lts");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        // 任务重试保存地址，默认用户目录下
//        jobClient.setJobInfoSavePath(Constants.USER_HOME);
        jobClient.setJobFinishedHandler(new JobFinishedHandlerImpl());
        jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
        jobClient.setLoadBalance("consistenthash");
        jobClient.setJobInfoSavePath("xx");
        jobClient.start();

        JobClientTest jobClientTest = new JobClientTest();
        jobClientTest.jobClient = jobClient;
        jobClientTest.startConsole();
    }

}
