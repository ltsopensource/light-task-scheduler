package com.lts.tasktracker.domain;

import com.lts.core.AppContext;
import com.lts.core.constant.Level;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.tasktracker.monitor.StopWorkingMonitor;
import com.lts.tasktracker.runner.RunnerFactory;
import com.lts.tasktracker.runner.RunnerPool;
import com.lts.tasktracker.support.JobPullMachine;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class TaskTrackerAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;
    // runner 线程池
    private RunnerPool runnerPool;
    //
    private RunnerFactory runnerFactory;
    // Pull Job Machine
    private JobPullMachine jobPullMachine;

    private StopWorkingMonitor stopWorkingMonitor;
    /**
     * 业务日志记录级别
     */
    private Level bizLogLevel;
    /**
     * 执行任务的class
     */
    private Class<?> jobRunnerClass;

    public StopWorkingMonitor getStopWorkingMonitor() {
        return stopWorkingMonitor;
    }

    public void setStopWorkingMonitor(StopWorkingMonitor stopWorkingMonitor) {
        this.stopWorkingMonitor = stopWorkingMonitor;
    }

    public RunnerPool getRunnerPool() {
        return runnerPool;
    }

    public void setRunnerPool(RunnerPool runnerPool) {
        this.runnerPool = runnerPool;
    }

    public Level getBizLogLevel() {
        return bizLogLevel;
    }

    public void setBizLogLevel(Level bizLogLevel) {
        this.bizLogLevel = bizLogLevel;
    }

    public Class<?> getJobRunnerClass() {
        return jobRunnerClass;
    }

    public void setJobRunnerClass(Class<?> jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    public void setRunnerFactory(RunnerFactory runnerFactory) {
        this.runnerFactory = runnerFactory;
    }

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }

    public JobPullMachine getJobPullMachine() {
        return jobPullMachine;
    }

    public void setJobPullMachine(JobPullMachine jobPullMachine) {
        this.jobPullMachine = jobPullMachine;
    }
}
