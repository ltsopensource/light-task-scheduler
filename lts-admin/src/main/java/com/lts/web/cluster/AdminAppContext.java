package com.lts.web.cluster;

import com.lts.biz.logger.JobLogger;
import com.lts.core.AppContext;
import com.lts.core.cluster.Node;
import com.lts.queue.*;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class AdminAppContext extends AppContext {

    private CronJobQueue cronJobQueue;

    private ExecutableJobQueue executableJobQueue;

    private ExecutingJobQueue executingJobQueue;

    private JobFeedbackQueue jobFeedbackQueue;

    private NodeGroupStore nodeGroupStore;

    private JobLogger jobLogger;

    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public CronJobQueue getCronJobQueue() {
        return cronJobQueue;
    }

    public void setCronJobQueue(CronJobQueue cronJobQueue) {
        this.cronJobQueue = cronJobQueue;
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

    public JobFeedbackQueue getJobFeedbackQueue() {
        return jobFeedbackQueue;
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }
}
