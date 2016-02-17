package com.lts.jobtracker;

import com.lts.biz.logger.JobLoggerDelegate;
import com.lts.core.cluster.AbstractServerNode;
import com.lts.core.cmd.HttpCmdServer;
import com.lts.core.spi.ServiceLoader;
import com.lts.jobtracker.channel.ChannelManager;
import com.lts.jobtracker.command.AddJobHttpCmd;
import com.lts.jobtracker.command.LoadJobHttpCmd;
import com.lts.jobtracker.domain.JobTrackerAppContext;
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
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerAppContext> {

    public JobTracker() {
        // 监控中心
        appContext.setMonitor(new JobTrackerMonitor(appContext));
        // channel 管理者
        appContext.setChannelManager(new ChannelManager());
        // JobClient 管理者
        appContext.setJobClientManager(new JobClientManager(appContext));
        // TaskTracker 管理者
        appContext.setTaskTrackerManager(new TaskTrackerManager(appContext));
        // 命令中心
        appContext.setHttpCmdServer(new HttpCmdServer(appContext.getConfig()));

        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(appContext));
        // 添加master节点变化监听器
        addMasterChangeListener(new JobTrackerMasterChangeListener(appContext));
    }

    @Override
    protected void beforeStart() {
        // injectRemotingServer
        appContext.setRemotingServer(remotingServer);
        appContext.setJobLogger(new JobLoggerDelegate(config));
        appContext.setExecutableJobQueue(ServiceLoader.load(ExecutableJobQueueFactory.class, config).getQueue(config));
        appContext.setExecutingJobQueue(ServiceLoader.load(ExecutingJobQueueFactory.class, config).getQueue(config));
        appContext.setCronJobQueue(ServiceLoader.load(CronJobQueueFactory.class, config).getQueue(config));
        appContext.setJobFeedbackQueue(ServiceLoader.load(JobFeedbackQueueFactory.class, config).getQueue(config));
        appContext.setNodeGroupStore(ServiceLoader.load(NodeGroupStoreFactory.class, config).getStore(config));
        appContext.setPreLoader(ServiceLoader.load(PreLoaderFactory.class, config).getPreLoader(appContext));
        appContext.setJobReceiver(new JobReceiver(appContext));
        appContext.setJobSender(new JobSender(appContext));

        registerCommand();
    }

    private void registerCommand() {
        // 先启动CommandCenter，中间看端口是否被占用
        appContext.getHttpCmdServer().start();
        // 设置command端口，会暴露到注册中心上
        node.setCommandPort(appContext.getHttpCmdServer().getPort());

        // 手动加载任务
        appContext.getHttpCmdServer().registerCommand(new LoadJobHttpCmd(appContext));
        // 添加任务
        appContext.getHttpCmdServer().registerCommand(new AddJobHttpCmd(appContext));
    }

    @Override
    protected void afterStart() {
        appContext.getChannelManager().start();

        appContext.getMonitor().start();
    }

    @Override
    protected void afterStop() {
        appContext.getChannelManager().stop();

        appContext.getMonitor().stop();

        appContext.getHttpCmdServer().stop();
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
    }

    /**
     * 设置反馈数据给JobClient的负载均衡算法
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        appContext.setOldDataHandler(oldDataHandler);
    }

}
