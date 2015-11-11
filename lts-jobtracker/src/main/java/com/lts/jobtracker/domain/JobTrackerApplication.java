package com.lts.jobtracker.domain;

import com.lts.biz.logger.JobLogger;
import com.lts.core.Application;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.jobtracker.channel.ChannelManager;
import com.lts.jobtracker.id.IdGenerator;
import com.lts.jobtracker.sender.JobSender;
import com.lts.jobtracker.support.JobReceiver;
import com.lts.jobtracker.support.OldDataHandler;
import com.lts.jobtracker.support.checker.ExecutingDeadJobChecker;
import com.lts.jobtracker.support.cluster.JobClientManager;
import com.lts.jobtracker.support.cluster.TaskTrackerManager;
import com.lts.queue.*;

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
    // store the connected node groups
    private NodeGroupStore nodeGroupStore;

    // Cron Job queue
    private CronJobQueue cronJobQueue;
    // feedback queue
    private JobFeedbackQueue jobFeedbackQueue;
    // job id generator
    private IdGenerator idGenerator;

    private PreLoader preLoader;

    private JobReceiver jobReceiver;

    private JobSender jobSender;

    public JobSender getJobSender() {
        return jobSender;
    }

    public void setJobSender(JobSender jobSender) {
        this.jobSender = jobSender;
    }

    public JobReceiver getJobReceiver() {
        return jobReceiver;
    }

    public void setJobReceiver(JobReceiver jobReceiver) {
        this.jobReceiver = jobReceiver;
    }

    public PreLoader getPreLoader() {
        return preLoader;
    }

    public void setPreLoader(PreLoader preLoader) {
        this.preLoader = preLoader;
    }

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

    public NodeGroupStore getNodeGroupStore() {
        return nodeGroupStore;
    }

    public void setNodeGroupStore(NodeGroupStore nodeGroupStore) {
        this.nodeGroupStore = nodeGroupStore;
    }
}
