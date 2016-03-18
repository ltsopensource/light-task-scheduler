package com.lts.spring.quartz;

import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.QuietUtils;
import com.lts.core.domain.Job;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.json.JSON;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.domain.Response;
import com.lts.tasktracker.TaskTracker;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzLTSProxyAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzLTSProxyAgent.class);
    private QuartzLTSConfig quartzLTSConfig;
    private List<QuartzCronJob> quartzCronJobs = new CopyOnWriteArrayList<QuartzCronJob>();
    private AtomicBoolean ready = new AtomicBoolean(false);

    public QuartzLTSProxyAgent(QuartzLTSConfig quartzLTSConfig) {
        this.quartzLTSConfig = quartzLTSConfig;
    }

    // 开始代理
    public void startProxy(List<QuartzCronJob> cronJobs) {
        if (CollectionUtils.isEmpty(cronJobs)) {
            return;
        }
        quartzCronJobs.addAll(cronJobs);

        if (!ready.compareAndSet(false, true)) {
            return;
        }
        new NamedThreadFactory(QuartzLTSProxyAgent.class.getSimpleName() + "_LazyStart").newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 3S之后启动 JobClient和TaskTracker, 为了防止有多个SchedulerFactoryBean, 从而这个方法被调用多次
                    QuietUtils.sleep(3000);
                    startProxy0();
                } catch (Throwable t) {
                    LOGGER.error("Error on start " + QuartzLTSProxyAgent.class.getSimpleName(), t);
                }
            }
        }).start();
    }

    private void startProxy0() {
        // 1. 先启动 TaskTracker
        startTaskTracker();

        // 2. 启动JobClient并提交任务
        JobClient jobClient = startJobClient();

        // 3. 提交任务
        submitJobs(jobClient);
    }

    private void startTaskTracker() {
        TaskTracker taskTracker = new TaskTracker();
        taskTracker.setRegistryAddress(quartzLTSConfig.getRegistryAddress());
        taskTracker.setClusterName(quartzLTSConfig.getClusterName());
        taskTracker.setNodeGroup(quartzLTSConfig.getTaskTrackerNodeGroup());
        taskTracker.setDataPath(quartzLTSConfig.getDataPath());
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
        JobClient jobClient = new JobClient();
        jobClient.setRegistryAddress(quartzLTSConfig.getRegistryAddress());
        jobClient.setClusterName(quartzLTSConfig.getClusterName());
        jobClient.setNodeGroup(quartzLTSConfig.getJobClientNodeGroup());
        jobClient.setDataPath(quartzLTSConfig.getDataPath());
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
            job.setSubmitNodeGroup(quartzLTSConfig.getJobClientNodeGroup());
            job.setTaskTrackerNodeGroup(quartzLTSConfig.getTaskTrackerNodeGroup());
            job.setReplaceOnExist(quartzLTSConfig.isReplaceOnExist());
            job.setParam("description", description);
            job.setNeedFeedback(false);

            jobs.add(job);
        }
        LOGGER.info("=============LTS=========== Submit start");
        submitJobs0(jobClient, jobs);
        LOGGER.info("=============LTS=========== Submit end");
    }

    private void submitJobs0(JobClient jobClient, List<Job> jobs) {
        List<Job> failedJobs = null;
        try {
            Response response = jobClient.submitJob(jobs);
            if (!response.isSuccess()) {
                LOGGER.warn("Submit Quartz Jobs to LTS failed: {}", JSON.toJSONString(response));
                failedJobs = response.getFailedJobs();
            }
        } catch (Throwable e) {
            LOGGER.warn("Submit Quartz Jobs to LTS error", e);
            failedJobs = jobs;
        }

        if (CollectionUtils.isNotEmpty(failedJobs)) {
            // 没提交成功要重试 3S 之后重试
            LOGGER.info("=============LTS=========== Sleep 3 Seconds and retry");
            QuietUtils.sleep(3000);
            submitJobs0(jobClient, failedJobs);
            return;
        }

        // 如果成功了, 关闭jobClient
        jobClient.stop();
    }
}
