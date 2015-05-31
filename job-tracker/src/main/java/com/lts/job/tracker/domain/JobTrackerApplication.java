package com.lts.job.tracker.domain;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.core.Application;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.queue.CronJobQueue;
import com.lts.job.queue.ExecutableJobQueue;
import com.lts.job.queue.ExecutingJobQueue;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.id.IdGenerator;
import com.lts.job.tracker.support.cluster.JobClientManager;
import com.lts.job.tracker.support.OldDataHandler;
import com.lts.job.tracker.support.cluster.TaskTrackerManager;
import com.lts.job.tracker.support.checker.ExecutingDeadJobChecker;

/**
 * JobTracker Application
 *
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobTrackerApplication extends Application {

    private RemotingServerDelegate remotingServer;
    // channel manager
    private ChannelManager channelManager;
    // JobClient manager for job tracker
    private JobClientManager jobClientManager;
    // TaskTracker manager for job tracker
    private TaskTrackerManager taskTrackerManager;
    // dead job checker
    private ExecutingDeadJobChecker executingDeadJobChecker;
    // old data handler, dirty data
    private OldDataHandler oldDataHandler;
    // biz logger
    private JobLogger jobLogger;

    // executable job queue（waiting for exec）
    private ExecutableJobQueue executableJobQueue;
    // executing job queue
    private ExecutingJobQueue executingJobQueue;

    // Cron Job queue
    private CronJobQueue cronJobQueue;
    // feedback queue
    private JobFeedbackQueue jobFeedbackQueue;
    // job id generator
    private IdGenerator idGenerator;

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public JobFeedbackQueue getJobFeedbackQueue() {
        return jobFeedbackQueue;
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }

    public RemotingServerDelegate getRemotingServer() {
        return remotingServer;
    }

    public void setRemotingServer(RemotingServerDelegate remotingServer) {
        this.remotingServer = remotingServer;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public void setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public JobClientManager getJobClientManager() {
        return jobClientManager;
    }

    public void setJobClientManager(JobClientManager jobClientManager) {
        this.jobClientManager = jobClientManager;
    }

    public TaskTrackerManager getTaskTrackerManager() {
        return taskTrackerManager;
    }

    public void setTaskTrackerManager(TaskTrackerManager taskTrackerManager) {
        this.taskTrackerManager = taskTrackerManager;
    }

    public ExecutingDeadJobChecker getExecutingDeadJobChecker() {
        return executingDeadJobChecker;
    }

    public void setExecutingDeadJobChecker(ExecutingDeadJobChecker executingDeadJobChecker) {
        this.executingDeadJobChecker = executingDeadJobChecker;
    }

    public OldDataHandler getOldDataHandler() {
        return oldDataHandler;
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        this.oldDataHandler = oldDataHandler;
    }

    public CronJobQueue getCronJobQueue() {
        return cronJobQueue;
    }

    public void setCronJobQueue(CronJobQueue cronJobQueue) {
        this.cronJobQueue = cronJobQueue;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ExecutableJobQueue getExecutableJobQueue() {
        return executableJobQueue;
    }

    public void setExecutableJobQueue(ExecutableJobQueue executableJobQueue) {
        this.executableJobQueue = executableJobQueue;
    }

    public ExecutingJobQueue getExecutingJobQueue() {
        return executingJobQueue;
    }

    public void setExecutingJobQueue(ExecutingJobQueue executingJobQueue) {
        this.executingJobQueue = executingJobQueue;
    }
}
