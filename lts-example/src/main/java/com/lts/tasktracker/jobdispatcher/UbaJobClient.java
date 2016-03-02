package com.lts.tasktracker.jobdispatcher;

import com.google.common.io.Resources;
import com.lts.core.domain.Job;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.example.support.BaseJobClientTest;
import com.lts.example.support.JobCompletedHandlerImpl;
import com.lts.example.support.MasterChangeListenerImpl;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/14.
 */
@SuppressWarnings("rawtypes")
public class UbaJobClient extends BaseJobClientTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerDispatcher.class);
    protected static Properties conf;

    public static void main(String[] args) throws IOException {
        submitJob();
    }

    public static Properties getConfig() {
        if (conf != null) {
            return conf;
        }
        URL url = Resources.getResource("tasktracker.cfg");
        conf = new Properties();
        try {
            conf.load(new FileInputStream(new File(url.toURI())));
        } catch (Exception e) {
            LOGGER.error("can not find tasktracker.cfg", e);
        }
        return conf;
    }
    public static String getConfig(String key) {
        return getConfig().get(key).toString();
    }
    public static void submitJob() {
        if("false".equalsIgnoreCase(getConfig("includeJobClient"))){
            return;
        }
        try {
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

    private static void submitJob(String product, JobClient jobClient) {
        Job job = buildJob(product, "action");
        Response response = jobClient.submitJob(job);
        LOGGER.debug(JSON.toJSONString(response));
    }

    private static Job buildJob(String product, String jobType) {
        Job job = new Job();
        job.setTaskId(product + "_" + jobType);
        job.setParam("product", product);
        job.setParam("type", jobType);
        job.setParam("size", "300");
        job.setTaskTrackerNodeGroup(getConfig("nodeGroup"));
        job.setNeedFeedback(false);
        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
        job.setCronExpression(getConfig("cron.default"));
        //job.setCronExpression("0 53/10 * * * ?");
        return job;
    }

}