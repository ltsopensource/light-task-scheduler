package com.lts.tasktracker.jobdispatcher;

import com.lts.core.domain.Job;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.example.support.JobCompletedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.util.Properties;

/**
 *用户行为分析 数据采集 初始任务
 */
@SuppressWarnings("rawtypes")
public class UbaJobSubmitter implements JobSubmitter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDispatcher.class);
    protected Properties conf;

    @Override
    public void submitJob(Properties conf) {
        try {
            this.conf = conf;
            // 推荐使用RetryJobClient
            JobClient jobClient = new RetryJobClient();
            jobClient.setNodeGroup(getConfig("nodeGroup"));
            jobClient.setClusterName(getConfig("clusterName"));
            jobClient.setRegistryAddress(getConfig("registryAddress"));
            jobClient.setJobFinishedHandler(new JobCompletedHandlerImpl());
            // master 节点变化监听器，当有集群中只需要一个节点执行某个事情的时候，可以监听这个事件
            jobClient.addMasterChangeListener(new MasterChangeListenerImpl());
            jobClient.start();

            submitJob("gaq", jobClient);
            submitJob("gyfc", jobClient);
            submitJob("ysg", jobClient);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected String getConfig(String key) {
        return conf.get(key).toString();
    }

    protected void submitJob(String product, JobClient jobClient) {
        Job job = buildJob(product, "action");
        Response response = jobClient.submitJob(job);
        LOGGER.debug(JSON.toJSONString(response));
    }

    protected Job buildJob(String product, String jobType) {
        Job job = new Job();
        job.setTaskId(product + "_" + jobType);
        job.setParam("product", product);
        job.setParam("type", jobType);
        job.setParam("size", "300");
        job.setTaskTrackerNodeGroup(getConfig("nodeGroup"));
        job.setNeedFeedback(false);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression(getConfig("mySubmitCronExpression"));
        //job.setCronExpression("0 53/10 * * * ?");
        return job;
    }
}