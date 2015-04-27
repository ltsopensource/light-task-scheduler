package com.lts.job.task.tracker.domain;

import com.lts.job.core.Application;
import com.lts.job.core.constant.Constants;
import com.lts.job.core.constant.Level;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.task.tracker.runner.RunnerFactory;
import com.lts.job.task.tracker.runner.RunnerPool;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class TaskTrackerApplication extends Application{

    private RemotingClientDelegate remotingClient;
    /**
     * runner 线程池
     */
    private RunnerPool runnerPool;

    private RunnerFactory runnerFactory;
    /**
     * 业务日志记录级别
     */
    private Level bizLogLevel;
    /**
     * 执行任务的class
     */
    private Class jobRunnerClass;
    /**
     * 可用线程个数
     */
    private Integer availableThreads = Constants.AVAILABLE_PROCESSOR;

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

    public Class getJobRunnerClass() {
        return jobRunnerClass;
    }

    public void setJobRunnerClass(Class jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public Integer getAvailableThreads() {
        return availableThreads;
    }

    public void setAvailableThreads(Integer availableThreads) {
        this.availableThreads = availableThreads;
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
}
