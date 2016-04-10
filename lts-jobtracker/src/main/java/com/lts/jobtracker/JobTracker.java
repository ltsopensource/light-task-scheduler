package com.lts.jobtracker;

import com.lts.biz.logger.SmartJobLogger;
import com.lts.core.cluster.AbstractServerNode;
import com.lts.core.spi.ServiceLoader;
import com.lts.jobtracker.channel.ChannelManager;
import com.lts.jobtracker.cmd.AddJobHttpCmd;
import com.lts.jobtracker.cmd.LoadJobHttpCmd;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.jobtracker.domain.JobTrackerNode;
import com.lts.jobtracker.monitor.JobTrackerMStatReporter;
import com.lts.jobtracker.processor.RemotingDispatcher;
import com.lts.jobtracker.sender.JobSender;
import com.lts.jobtracker.support.JobReceiver;
import com.lts.jobtracker.support.NonRelyOnPrevCycleJobScheduler;
import com.lts.jobtracker.support.OldDataHandler;
import com.lts.jobtracker.support.checker.ExecutableDeadJobChecker;
import com.lts.jobtracker.support.checker.ExecutingDeadJobChecker;
import com.lts.jobtracker.support.checker.FeedbackJobSendChecker;
import com.lts.jobtracker.support.cluster.JobClientManager;
import com.lts.jobtracker.support.cluster.TaskTrackerManager;
import com.lts.jobtracker.support.listener.JobNodeChangeListener;
import com.lts.jobtracker.support.listener.JobTrackerMasterChangeListener;
import com.lts.jobtracker.support.policy.OldDataDeletePolicy;
import com.lts.queue.JobQueueFactory;
import com.lts.remoting.RemotingProcessor;

/**
 * @author Robert HG (254963746@qq.com) on 7/23/14.
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerAppContext> {

    public JobTracker() {
        // 监控中心
        appContext.setMStatReporter(new JobTrackerMStatReporter(appContext));
        // channel 管理者
        appContext.setChannelManager(new ChannelManager());
        // JobClient 管理者
        appContext.setJobClientManager(new JobClientManager(appContext));
        // TaskTracker 管理者
        appContext.setTaskTrackerManager(new TaskTrackerManager(appContext));
        // 添加节点变化监听器
        addNodeChangeListener(new JobNodeChangeListener(appContext));
        // 添加master节点变化监听器
        addMasterChangeListener(new JobTrackerMasterChangeListener(appContext));
    }

    @Override
    protected void beforeStart() {
        // injectRemotingServer
        appContext.setRemotingServer(remotingServer);
        appContext.setJobLogger(new SmartJobLogger(appContext));

        JobQueueFactory factory = ServiceLoader.load(JobQueueFactory.class, config);

        appContext.setExecutableJobQueue(factory.getExecutableJobQueue(config));
        appContext.setExecutingJobQueue(factory.getExecutingJobQueue(config));
        appContext.setCronJobQueue(factory.getCronJobQueue(config));
        appContext.setRepeatJobQueue(factory.getRepeatJobQueue(config));
        appContext.setSuspendJobQueue(factory.getSuspendJobQueue(config));
        appContext.setJobFeedbackQueue(factory.getJobFeedbackQueue(config));
        appContext.setNodeGroupStore(factory.getNodeGroupStore(config));
        appContext.setPreLoader(factory.getPreLoader(appContext));
        appContext.setJobReceiver(new JobReceiver(appContext));
        appContext.setJobSender(new JobSender(appContext));
        appContext.setNonRelyOnPrevCycleJobScheduler(new NonRelyOnPrevCycleJobScheduler(appContext));
        appContext.setExecutableDeadJobChecker(new ExecutableDeadJobChecker(appContext));
        appContext.setExecutingDeadJobChecker(new ExecutingDeadJobChecker(appContext));
        appContext.setFeedbackJobSendChecker(new FeedbackJobSendChecker(appContext));

        appContext.getHttpCmdServer().registerCommands(
                new LoadJobHttpCmd(appContext),     // 手动加载任务
                new AddJobHttpCmd(appContext));     // 添加任务

        if(appContext.getOldDataHandler() == null){
            appContext.setOldDataHandler(new OldDataDeletePolicy());
        }
    }

    @Override
    protected void afterStart() {
        appContext.getChannelManager().start();
        appContext.getMStatReporter().start();
    }

    @Override
    protected void afterStop() {
        appContext.getChannelManager().stop();
        appContext.getMStatReporter().stop();
        appContext.getHttpCmdServer().stop();
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        appContext.setOldDataHandler(oldDataHandler);
    }

}
