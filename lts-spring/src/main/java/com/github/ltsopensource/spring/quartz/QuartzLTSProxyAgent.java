package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.QuietUtils;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.jobclient.domain.Response;
import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.TaskTrackerBuilder;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.github.ltsopensource.tasktracker.runner.RunnerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

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
    private List<QuartzJobContext> quartzJobContexts = new CopyOnWriteArrayList<QuartzJobContext>();
    private AtomicBoolean ready = new AtomicBoolean(false);

    public QuartzLTSProxyAgent(QuartzLTSConfig quartzLTSConfig) {
        this.quartzLTSConfig = quartzLTSConfig;
    }

    // 开始代理
    public void startProxy(List<QuartzJobContext> cronJobs) {
        if (CollectionUtils.isEmpty(cronJobs)) {
            return;
        }
        quartzJobContexts.addAll(cronJobs);

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

        TaskTracker taskTracker = TaskTrackerBuilder.buildByProperties(quartzLTSConfig.getTaskTrackerProperties());
        taskTracker.setWorkThreads(quartzJobContexts.size());
        taskTracker.setJobRunnerClass(QuartzJobRunnerDispatcher.class);

        final QuartzJobRunnerDispatcher jobRunnerDispatcher = new QuartzJobRunnerDispatcher(quartzJobContexts);
        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return jobRunnerDispatcher;
            }
        });

        taskTracker.start();
    }

    private JobClient startJobClient() {
        JobClientProperties jobClientProperties = quartzLTSConfig.getJobClientProperties();
        jobClientProperties.setUseRetryClient(false);
        JobClient jobClient = JobClientBuilder.buildByProperties(jobClientProperties);
        jobClient.start();
        return jobClient;
    }

    private void submitJobs(JobClient jobClient) {

        List<Job> jobs = new ArrayList<Job>(quartzJobContexts.size());
        for (QuartzJobContext quartzJobContext : quartzJobContexts) {

            if (QuartzJobType.CRON == quartzJobContext.getType()) {
                jobs.add(buildCronJob(quartzJobContext));
            } else if (QuartzJobType.SIMPLE_REPEAT == quartzJobContext.getType()) {
                jobs.add(buildSimpleJob(quartzJobContext));
            }
        }
        LOGGER.info("=============LTS=========== Submit start");
        submitJobs0(jobClient, jobs);
        LOGGER.info("=============LTS=========== Submit end");
    }

    private Job buildCronJob(QuartzJobContext quartzJobContext) {

        CronTriggerImpl cronTrigger = (CronTriggerImpl) quartzJobContext.getTrigger();
        String cronExpression = cronTrigger.getCronExpression();
        String description = cronTrigger.getDescription();
        int priority = cronTrigger.getPriority();
        String name = quartzJobContext.getName();

        Job job = new Job();
        job.setTaskId(name);
        job.setPriority(priority);
        job.setCronExpression(cronExpression);
        job.setSubmitNodeGroup(quartzLTSConfig.getJobClientProperties().getNodeGroup());
        job.setTaskTrackerNodeGroup(quartzLTSConfig.getTaskTrackerProperties().getNodeGroup());
        job.setParam("description", description);
        setJobProp(job);

        return job;
    }

    private Job buildSimpleJob(QuartzJobContext quartzJobContext) {

        SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) quartzJobContext.getTrigger();

        String description = simpleTrigger.getDescription();
        int priority = simpleTrigger.getPriority();
        String name = quartzJobContext.getName();
        int repeatCount = simpleTrigger.getRepeatCount();
        long repeatInterval = simpleTrigger.getRepeatInterval();

        Job job = new Job();
        job.setTaskId(name);

        job.setTriggerDate(simpleTrigger.getNextFireTime());
        job.setRepeatCount(repeatCount);

        if (repeatCount != 0) {
            job.setRepeatInterval(repeatInterval);
        }
        job.setPriority(priority);
        job.setSubmitNodeGroup(quartzLTSConfig.getJobClientProperties().getNodeGroup());
        job.setTaskTrackerNodeGroup(quartzLTSConfig.getTaskTrackerProperties().getNodeGroup());
        job.setParam("description", description);
        setJobProp(job);

        return job;
    }

    private void setJobProp(Job job) {
        QuartzLTSConfig.JobProperties jobProperties = quartzLTSConfig.getJobProperties();
        if (jobProperties == null) {
            return;
        }
        if (jobProperties.getMaxRetryTimes() != null) {
            job.setMaxRetryTimes(jobProperties.getMaxRetryTimes());
        }
        if (jobProperties.getNeedFeedback() != null) {
            job.setNeedFeedback(jobProperties.getNeedFeedback());
        }
        if (jobProperties.getRelyOnPrevCycle() != null) {
            job.setRelyOnPrevCycle(jobProperties.getRelyOnPrevCycle());
        }
        if (jobProperties.getReplaceOnExist() != null) {
            job.setReplaceOnExist(jobProperties.getReplaceOnExist());
        }
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
