package com.github.ltsopensource.admin.cluster;

import com.github.ltsopensource.admin.access.face.*;
import com.github.ltsopensource.admin.access.memory.NodeMemCacheAccess;
import com.github.ltsopensource.admin.web.support.NoRelyJobGenerator;
import com.github.ltsopensource.biz.logger.JobLogger;
import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.queue.*;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BackendAppContext extends AppContext {

    private CronJobQueue cronJobQueue;
    private RepeatJobQueue repeatJobQueue;
    private ExecutableJobQueue executableJobQueue;
    private ExecutingJobQueue executingJobQueue;
    private JobFeedbackQueue jobFeedbackQueue;
    private SuspendJobQueue suspendJobQueue;
    private NodeGroupStore nodeGroupStore;
    private JobLogger jobLogger;
    private Node node;

    private BackendJobClientMAccess backendJobClientMAccess;
    private BackendJobTrackerMAccess backendJobTrackerMAccess;
    private BackendTaskTrackerMAccess backendTaskTrackerMAccess;
    private BackendJVMGCAccess backendJVMGCAccess;
    private BackendJVMMemoryAccess backendJVMMemoryAccess;
    private BackendJVMThreadAccess backendJVMThreadAccess;
    private BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess;

    private NodeMemCacheAccess nodeMemCacheAccess;

    private NoRelyJobGenerator noRelyJobGenerator;

    private BackendRegistrySrv backendRegistrySrv;

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

    public RepeatJobQueue getRepeatJobQueue() {
        return repeatJobQueue;
    }

    public void setRepeatJobQueue(RepeatJobQueue repeatJobQueue) {
        this.repeatJobQueue = repeatJobQueue;
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

    public SuspendJobQueue getSuspendJobQueue() {
        return suspendJobQueue;
    }

    public void setSuspendJobQueue(SuspendJobQueue suspendJobQueue) {
        this.suspendJobQueue = suspendJobQueue;
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

    public BackendJobClientMAccess getBackendJobClientMAccess() {
        return backendJobClientMAccess;
    }

    public void setBackendJobClientMAccess(BackendJobClientMAccess backendJobClientMAccess) {
        this.backendJobClientMAccess = backendJobClientMAccess;
    }

    public BackendJobTrackerMAccess getBackendJobTrackerMAccess() {
        return backendJobTrackerMAccess;
    }

    public void setBackendJobTrackerMAccess(BackendJobTrackerMAccess backendJobTrackerMAccess) {
        this.backendJobTrackerMAccess = backendJobTrackerMAccess;
    }

    public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess() {
        return backendTaskTrackerMAccess;
    }

    public void setBackendTaskTrackerMAccess(BackendTaskTrackerMAccess backendTaskTrackerMAccess) {
        this.backendTaskTrackerMAccess = backendTaskTrackerMAccess;
    }

    public BackendJVMGCAccess getBackendJVMGCAccess() {
        return backendJVMGCAccess;
    }

    public void setBackendJVMGCAccess(BackendJVMGCAccess backendJVMGCAccess) {
        this.backendJVMGCAccess = backendJVMGCAccess;
    }

    public BackendJVMMemoryAccess getBackendJVMMemoryAccess() {
        return backendJVMMemoryAccess;
    }

    public void setBackendJVMMemoryAccess(BackendJVMMemoryAccess backendJVMMemoryAccess) {
        this.backendJVMMemoryAccess = backendJVMMemoryAccess;
    }

    public BackendJVMThreadAccess getBackendJVMThreadAccess() {
        return backendJVMThreadAccess;
    }

    public void setBackendJVMThreadAccess(BackendJVMThreadAccess backendJVMThreadAccess) {
        this.backendJVMThreadAccess = backendJVMThreadAccess;
    }

    public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess() {
        return backendNodeOnOfflineLogAccess;
    }

    public void setBackendNodeOnOfflineLogAccess(BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess) {
        this.backendNodeOnOfflineLogAccess = backendNodeOnOfflineLogAccess;
    }

    public NodeMemCacheAccess getNodeMemCacheAccess() {
        return nodeMemCacheAccess;
    }

    public void setNodeMemCacheAccess(NodeMemCacheAccess nodeMemCacheAccess) {
        this.nodeMemCacheAccess = nodeMemCacheAccess;
    }

    public NoRelyJobGenerator getNoRelyJobGenerator() {
        return noRelyJobGenerator;
    }

    public void setNoRelyJobGenerator(NoRelyJobGenerator noRelyJobGenerator) {
        this.noRelyJobGenerator = noRelyJobGenerator;
    }

    public BackendRegistrySrv getBackendRegistrySrv() {
        return backendRegistrySrv;
    }

    public void setBackendRegistrySrv(BackendRegistrySrv backendRegistrySrv) {
        this.backendRegistrySrv = backendRegistrySrv;
    }
}
