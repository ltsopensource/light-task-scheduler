package com.github.ltsopensource.jobtracker.support.listener;

import com.github.ltsopensource.core.cluster.Node;
import com.github.ltsopensource.core.listener.MasterChangeListener;
import com.github.ltsopensource.jobtracker.domain.JobTrackerAppContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/24/14.
 *         JobTracker master 节点变化之后
 */
public class JobTrackerMasterChangeListener implements MasterChangeListener {

    private JobTrackerAppContext appContext;

    public JobTrackerMasterChangeListener(JobTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void change(Node master, boolean isMaster) {

        if (appContext.getConfig().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
            appContext.getFeedbackJobSendChecker().start();
            appContext.getExecutableDeadJobChecker().start();
            appContext.getExecutingDeadJobChecker().start();
            appContext.getNonRelyOnPrevCycleJobScheduler().start();
        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
            appContext.getFeedbackJobSendChecker().stop();
            appContext.getExecutableDeadJobChecker().stop();
            appContext.getExecutingDeadJobChecker().stop();
            appContext.getNonRelyOnPrevCycleJobScheduler().stop();
        }
    }
}
