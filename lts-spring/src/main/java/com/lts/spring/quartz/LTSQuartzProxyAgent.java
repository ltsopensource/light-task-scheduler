package com.lts.spring.quartz;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.domain.Job;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.domain.Response;
import com.lts.tasktracker.TaskTracker;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class LTSQuartzProxyAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSQuartzProxyAgent.class);
    private LTSQuartzConfig ltsQuartzConfig;
    private List<QuartzCronJob> quartzCronJobs = new CopyOnWriteArrayList<QuartzCronJob>();
    private volatile boolean already = false;

    public LTSQuartzProxyAgent(LTSQuartzConfig ltsQuartzConfig) {
        this.ltsQuartzConfig = ltsQuartzConfig;
    }

    // 开始代理
    public void startProxy(List<QuartzCronJob> cronJobs) {
        if (CollectionUtils.isEmpty(cronJobs)) {
            return;
        }
        quartzCronJobs.addAll(cronJobs);

        if (already) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        // 3S之后启动 JobClient和TaskTracker, 为了防止有多个SchedulerFactoryBean, 从而这个方法被调用多次
                        Thread.sleep(3000L);
                    } catch (InterruptedException ignored) {
                    }
                    startProxy0();
                } catch (Throwable t) {
                    LOGGER.error("Error on start LTSQuartzProxyAgent", t);
                }
            }
        }).start();

        already = true;
    }

    private void startProxy0() {
        // 1. 先启动 TaskTracker
        startTaskTracker();

        // 2. 启动JobClient并提交任务
        JobClient jobClient = startJobClient();

        // 3. 提交任务
        submitJobs(jobClient);

        // 4. 关闭jobClient
        jobClient.stop();
    }

    private void startTaskTracker() {
        TaskTracker taskTracker = new TaskTracker();
        taskTracker.setRegistryAddress(ltsQuartzConfig.getRegistryAddress());
        taskTracker.setClusterName(ltsQuartzConfig.getClusterName());
        taskTracker.setNodeGroup(ltsQuartzConfig.getTaskTrackerNodeGroup());
        taskTracker.setDataPath(ltsQuartzConfig.getDataPath());
        taskTracker.setWorkThreads(quartzCronJobs.size());
        taskTracker.setJobRunnerClass(QuartzJobRunnerDispatcher.class);

        final QuartzJobRunnerDispatcher jobRunnerDispatcher = new QuartzJobRunnerDispatcher(quartzCronJobs);
        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return jobRunnerDispatcher;
            }
        });

        taskTracker.start();
    }

    private JobClient startJobClient() {
        JobClient jobClient = new RetryJobClient();
        jobClient.setRegistryAddress(ltsQuartzConfig.getRegistryAddress());
        jobClient.setClusterName(ltsQuartzConfig.getClusterName());
        jobClient.setNodeGroup(ltsQuartzConfig.getJobClientNodeGroup());
        jobClient.setDataPath(ltsQuartzConfig.getDataPath());
        jobClient.start();
        return jobClient;
    }

    private void submitJobs(JobClient jobClient) {
        List<Job> jobs = new ArrayList<Job>(quartzCronJobs.size());
        for (QuartzCronJob quartzCronJob : quartzCronJobs) {

            CronTriggerImpl cronTrigger = quartzCronJob.getCronTrigger();
            String cronExpression = cronTrigger.getCronExpression();
            String description = cronTrigger.getDescription();
            int priority = cronTrigger.getPriority();
            String name = cronTrigger.getName();

            Job job = new Job();
            job.setTaskId(name);
            job.setPriority(priority);
            job.setCronExpression(cronExpression);
            job.setSubmitNodeGroup(ltsQuartzConfig.getJobClientNodeGroup());
            job.setTaskTrackerNodeGroup(ltsQuartzConfig.getTaskTrackerNodeGroup());
            job.setReplaceOnExist(true);
            job.setParam("description", description);
            job.setNeedFeedback(false);

            jobs.add(job);
        }

        Response response = jobClient.submitJob(jobs);
        if (!response.isSuccess()) {
            LOGGER.warn("Submit Quartz Jobs to LTS failed: {}", JSON.toJSONString(response));
        }
    }
}
