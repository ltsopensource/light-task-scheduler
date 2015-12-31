package com.lts.jobtracker;

import com.lts.biz.logger.JobLoggerDelegate;
import com.lts.core.command.HttpCommandCenter;
import com.lts.core.command.HttCommands;
import com.lts.core.cluster.AbstractServerNode;
import com.lts.core.spi.ServiceLoader;
import com.lts.jobtracker.channel.ChannelManager;
import com.lts.jobtracker.command.AddJobHttpCommand;
import com.lts.jobtracker.command.LoadJobHttpCommand;
import com.lts.jobtracker.domain.JobTrackerApplication;
import com.lts.jobtracker.domain.JobTrackerNode;
import com.lts.jobtracker.monitor.JobTrackerMonitor;
import com.lts.jobtracker.processor.RemotingDispatcher;
import com.lts.jobtracker.sender.JobSender;
import com.lts.jobtracker.support.JobReceiver;
import com.lts.jobtracker.support.OldDataHandler;
import com.lts.jobtracker.support.cluster.JobClientManager;
import com.lts.jobtracker.support.cluster.TaskTrackerManager;
import com.lts.jobtracker.support.listener.JobNodeChangeListener;
import com.lts.jobtracker.support.listener.JobTrackerMasterChangeListener;
import com.lts.queue.*;
import com.lts.remoting.RemotingProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerApplication> {

    public JobTracker() {
        // 监控中心
        application.setMonitor(new JobTrackerMonitor(application));
        // channel 管理者
        application.setChannelManager(new ChannelManager());
        // JobClient 管理者
        application.setJobClientManager(new JobClientManager(application));
        // TaskTracker 管理者
        application.setTaskTrackerManager(new TaskTrackerManager(application));
        // 命令中心
        application.setHttpCommandCenter(new HttpCommandCenter(application.getConfig()));

        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(application));
        // 添加master节点变化监听器
        addMasterChangeListener(new JobTrackerMasterChangeListener(application));
    }

    @Override
    protected void beforeStart() {
        // injectRemotingServer
        application.setRemotingServer(remotingServer);
        application.setJobLogger(new JobLoggerDelegate(config));
        application.setExecutableJobQueue(ServiceLoader.load(ExecutableJobQueueFactory.class,config).getQueue(config));
        application.setExecutingJobQueue(ServiceLoader.load(ExecutingJobQueueFactory.class,config).getQueue(config));
        application.setCronJobQueue(ServiceLoader.load(CronJobQueueFactory.class, config).getQueue(config));
        application.setJobFeedbackQueue(ServiceLoader.load(JobFeedbackQueueFactory.class, config).getQueue(config));
        application.setNodeGroupStore(ServiceLoader.load(NodeGroupStoreFactory.class, config).getStore(config));
        application.setPreLoader(ServiceLoader.load(PreLoaderFactory.class, config).getPreLoader(application));
        application.setJobReceiver(new JobReceiver(application));
        application.setJobSender(new JobSender(application));

        registerCommand();
    }

    private void registerCommand() {
        // 先启动CommandCenter，中间看端口是否被占用
        application.getHttpCommandCenter().start();
        // 设置command端口，会暴露到注册中心上
        node.setCommandPort(application.getHttpCommandCenter().getPort());

        // 手动加载任务
        application.getHttpCommandCenter().registerCommand(HttCommands.LOAD_JOB, new LoadJobHttpCommand(application));
        // 添加任务
        application.getHttpCommandCenter().registerCommand(HttCommands.ADD_JOB, new AddJobHttpCommand(application));
    }

    @Override
    protected void afterStart() {
        application.getChannelManager().start();

        application.getMonitor().start();
    }

    @Override
    protected void afterStop() {
        application.getChannelManager().stop();

        application.getMonitor().stop();

        application.getHttpCommandCenter().stop();
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(application);
    }

    /**
     * 设置反馈数据给JobClient的负载均衡算法
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        application.setOldDataHandler(oldDataHandler);
    }

}
