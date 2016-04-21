package com.github.ltsopensource.example.api;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.example.support.BaseJobClientTest;
import com.github.ltsopensource.example.support.JobCompletedHandlerImpl;
import com.github.ltsopensource.example.support.MasterChangeListenerImpl;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.jobclient.RetryJobClient;
import com.github.ltsopensource.jobclient.domain.Response;

import java.io.IOException;

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
//        submitWidthNonRelyOnPrevCycle();
    }

    public static void submitWidthReplaceOnExist() throws IOException {
        JobClient jobClient = new JobClientBuilder()
                .setPropertiesConfigure(new String[]{"application.properties"})
                .setJobCompletedHandler(new JobCompletedHandlerImpl())
//                .addMasterChangeListener(new MasterChangeListenerImpl())
                .build();
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

    public static void submitWidthNonRelyOnPrevCycle() throws IOException {
        // 推荐使用RetryJobClient
        JobClient jobClient = new RetryJobClient();
        jobClient.setNodeGroup("test_jobClient");
        jobClient.setClusterName("test_cluster");
        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
        jobClient.setJobCompletedHandler(new JobCompletedHandlerImpl());
        jobClient.start();

        Job job = new Job();
        job.setTaskId("t_test_344444");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(false);
        job.setCronExpression("0/30 * * * * ?");
//        job.setRepeatInterval(30 * 1000L);
//        job.setRepeatCount(25);
        job.setRelyOnPrevCycle(false);
//        job.setTriggerTime(DateUtils.addDay(new Date(), 1));
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    public static void cancelJob() {
        JobClient jobClient = new JobClientBuilder()
                .setPropertiesConfigure(new String[]{"application.properties"})
                .setJobCompletedHandler(new JobCompletedHandlerImpl())
//                .addMasterChangeListener(new MasterChangeListenerImpl())
                .build();
        jobClient.start();

        jobClient.cancelJob("t_4", "test_trade_TaskTracker");
    }


    public static void console() throws IOException {

        JobClient jobClient = new JobClientBuilder()
                .setPropertiesConfigure(new String[]{"application.properties"})
                .setJobCompletedHandler(new JobCompletedHandlerImpl())
                .addMasterChangeListener(new MasterChangeListenerImpl())
                .build();
        jobClient.start();

        JobClientTest jobClientTest = new JobClientTest();
        jobClientTest.jobClient = jobClient;
        jobClientTest.startConsole();
    }

}