package com.github.ltsopensource.example.java;

import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.jobclient.domain.Response;

import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class Main {

    public static void main(String[] args) {

        // 方式1
//        JobClient jobClient = new RetryJobClient();
//        jobClient.setNodeGroup("test_jobClient");
//        jobClient.setClusterName("test_cluster");
//        jobClient.setRegistryAddress("zookeeper://127.0.0.1:2181");
//        jobClient.setJobCompletedHandler(new JobCompletedHandlerImpl());
//        jobClient.addConfig("job.fail.store", "mapdb");
//        jobClient.start();

        // 方式2
        JobClient jobClient = new JobClientBuilder()
                .setPropertiesConfigure("lts.properties")
                .setJobCompletedHandler(new JobCompletedHandlerImpl())
                .build();

        jobClient.start();

        submitCronJob(jobClient);
        submitRepeatJob(jobClient);
        submitRealtimeJob(jobClient);
        submitTriggerTimeJob(jobClient);
    }

    private static void submitCronJob(JobClient jobClient) {
        Job job = new Job();
        job.setTaskId("t_cron_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");      // 执行要执行该任务的taskTracker的节点组名称
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression("0 0/1 * * * ?");
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    private static void submitRepeatJob(JobClient jobClient) {
        Job job = new Job();
        job.setTaskId("t_repeat_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setRepeatCount(50);             // 一共执行50次
        job.setRepeatInterval(50 * 1000L);  // 50s 执行一次
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    private static void submitRealtimeJob(JobClient jobClient) {
        Job job = new Job();
        job.setTaskId("t_realtime_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

    private static void submitTriggerTimeJob(JobClient jobClient) {
        Job job = new Job();
        job.setTaskId("t_trigger_time_555");
        job.setParam("shopId", "1122222221");
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        job.setNeedFeedback(true);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setTriggerTime(DateUtils.addHour(new Date(), 1).getTime());   // 1 小时之后执行
        Response response = jobClient.submitJob(job);
        System.out.println(response);
    }

}
