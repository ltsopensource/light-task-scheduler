package com.lts.example.jobdispatcher;

import com.glodon.ysg.uba.UbaApp;
import com.glodon.ysg.uba.beidou.pojo.JobType;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.example.support.BaseJobClientTest;
import com.lts.example.support.JobCompletedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.io.IOException;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
@SuppressWarnings("rawtypes")
public class UbaJobClient extends BaseJobClientTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDispatcher.class);
    public static void main(String[] args) throws IOException {
        submitJob();
    }

    public static void submitJob() {
        try {
            // 推荐使用RetryJobClient
            JobClient jobClient = new RetryJobClient();
            jobClient.setNodeGroup(UbaApp.getAppConfig().get("nodeGroup"));
            jobClient.setClusterName(UbaApp.getAppConfig().get("clusterName"));
            jobClient.setRegistryAddress(UbaApp.getAppConfig().get("registryAddress"));
            jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
            // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
            jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
            jobClient.start();

            submitJob("gaq", jobClient);
            submitJob("gyfc", jobClient);
            submitJob("gmj", jobClient);
            submitJob("gbcb", jobClient);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

    }

    private static void submitJob(String product, JobClient jobClient) {
        Job job = buildJob(product, JobType.START_INFOS);
        Response response = jobClient.submitJob(job);
        System.out.println(response);
        job = buildJob(product, JobType.FUNCTION_INFOS);
        response = jobClient.submitJob(job);
        System.out.println(response);
        job = buildJob(product, JobType.STOP_INFOS);
        response = jobClient.submitJob(job);
        System.out.println(response);
        job = buildJob(product, JobType.VERSION_CHANGES);
        response = jobClient.submitJob(job);
        System.out.println(response);

    }

    private static Job buildJob(String product, String jobType) {
        Job job = new Job();
        job.setTaskId(product +"_"+ jobType);
        job.setParam("product", product);
        job.setParam("type", jobType);
        job.setParam("size", "300");
        job.setTaskTrackerNodeGroup(UbaApp.getAppConfig().get("nodeGroup"));
        job.setNeedFeedback(false);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression("0 7 3-4 * * ?");
        //job.setCronExpression("0 53/10 * * * ?");
        return job;
    }

}