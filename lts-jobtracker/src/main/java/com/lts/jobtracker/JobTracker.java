package com.lts.jobtracker;

import com.lts.biz.logger.JobLoggerDelegate;
import com.lts.command.CommandCenter;
import com.lts.command.Commands;
import com.lts.core.cluster.AbstractServerNode;
import com.lts.core.extension.ExtensionLoader;
import com.lts.jobtracker.channel.ChannelManager;
import com.lts.jobtracker.command.AddJobCommand;
import com.lts.jobtracker.command.LoadJobCommand;
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

    private CronJobQueueFactory cronJobQueueFactory =
            ExtensionLoader.getExtensionLoader(CronJobQueueFactory.class).getAdaptiveExtension();
    private ExecutableJobQueueFactory executableJobQueueFactory =
            ExtensionLoader.getExtensionLoader(ExecutableJobQueueFactory.class).getAdaptiveExtension();
    private ExecutingJobQueueFactory executingJobQueueFactory =
            ExtensionLoader.getExtensionLoader(ExecutingJobQueueFactory.class).getAdaptiveExtension();
    private JobFeedbackQueueFactory jobFeedbackQueueFactory =
            ExtensionLoader.getExtensionLoader(JobFeedbackQueueFactory.class).getAdaptiveExtension();
    private NodeGroupStoreFactory nodeGroupStoreFactory =
            ExtensionLoader.getExtensionLoader(NodeGroupStoreFactory.class).getAdaptiveExtension();
    private PreLoaderFactory preLoaderFactory =
            ExtensionLoader.getExtensionLoader(PreLoaderFactory.class).getAdaptiveExtension();

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
        application.setCommandCenter(new CommandCenter(application.getConfig()));

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
        application.setExecutableJobQueue(executableJobQueueFactory.getQueue(config));
        application.setExecutingJobQueue(executingJobQueueFactory.getQueue(config));
        application.setCronJobQueue(cronJobQueueFactory.getQueue(config));
        application.setJobFeedbackQueue(jobFeedbackQueueFactory.getQueue(config));
        application.setNodeGroupStore(nodeGroupStoreFactory.getStore(config));
        application.setPreLoader(preLoaderFactory.getPreLoader(config, application));
        application.setJobReceiver(new JobReceiver(application));
        application.setJobSender(new JobSender(application));

        registerCommand();
    }

    private void registerCommand() {
        // 先启动CommandCenter，中间看端口是否被占用
        application.getCommandCenter().start();
        // 设置command端口，会暴露到注册中心上
        node.setCommandPort(application.getCommandCenter().getPort());

        // 手动加载任务
        application.getCommandCenter().registerCommand(Commands.LOAD_JOB, new LoadJobCommand(application));
        // 添加任务
        application.getCommandCenter().registerCommand(Commands.ADD_JOB, new AddJobCommand(application));
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

        application.getCommandCenter().stop();
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
