package com.lts.job.tracker.domain;

import com.lts.job.biz.logger.JobLogger;
import com.lts.job.core.Application;
import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.queue.JobFeedbackQueue;
import com.lts.job.queue.JobQueue;
import com.lts.job.tracker.channel.ChannelManager;
import com.lts.job.tracker.support.JobClientManager;
import com.lts.job.tracker.support.OldDataHandler;
import com.lts.job.tracker.support.TaskTrackerManager;
import com.lts.job.tracker.support.checker.DeadJobChecker;

/**
 * JobTracker Application
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class JobTrackerApplication extends Application {

    private RemotingServerDelegate remotingServer;
    // channel 管理器
    private ChannelManager channelManager;
    // JobClient 节点管理器
    private JobClientManager jobClientManager;
    // TaskTracker 节点管理器
    private TaskTrackerManager taskTrackerManager;
    // 死任务检查器
    private DeadJobChecker deadJobChecker;
    // 对老数据的处理(脏数据)
    private OldDataHandler oldDataHandler;
    // 业务日志记录
    private JobLogger jobLogger;
    // 任务队列
    private JobQueue jobQueue;
    // 反馈任务队列
    private JobFeedbackQueue jobFeedbackQueue;

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public JobQueue getJobQueue() {
        return jobQueue;
    }

    public void setJobQueue(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
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

    public DeadJobChecker getDeadJobChecker() {
        return deadJobChecker;
    }

    public void setDeadJobChecker(DeadJobChecker deadJobChecker) {
        this.deadJobChecker = deadJobChecker;
    }

    public OldDataHandler getOldDataHandler() {
        return oldDataHandler;
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        this.oldDataHandler = oldDataHandler;
    }
}
