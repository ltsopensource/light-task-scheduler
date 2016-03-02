package com.lts.jobtracker.support.listener;

import com.lts.core.cluster.Node;
import com.lts.core.listener.MasterChangeListener;
import com.lts.jobtracker.domain.JobTrackerAppContext;
import com.lts.jobtracker.support.checker.ExecutableDeadJobChecker;
import com.lts.jobtracker.support.checker.ExecutingDeadJobChecker;
import com.lts.jobtracker.support.checker.FeedbackJobSendChecker;

/**
 * @author Robert HG (254963746@qq.com) on 8/24/14.
 *         JobTracker master 节点变化之后
 */
public class JobTrackerMasterChangeListener implements MasterChangeListener {

    private JobTrackerAppContext appContext;
    private ExecutingDeadJobChecker executingDeadJobChecker;
    private FeedbackJobSendChecker feedbackJobSendChecker;
    private ExecutableDeadJobChecker executableDeadJobChecker;

    public JobTrackerMasterChangeListener(JobTrackerAppContext appContext) {
        this.appContext = appContext;
        this.executingDeadJobChecker = new ExecutingDeadJobChecker(appContext);
        this.appContext.setExecutingDeadJobChecker(executingDeadJobChecker);
        this.feedbackJobSendChecker = new FeedbackJobSendChecker(appContext);
        this.executableDeadJobChecker = new ExecutableDeadJobChecker(appContext);
    }

    @Override
    public void change(Node master, boolean isMaster) {

        if (appContext.getConfig().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
            feedbackJobSendChecker.start();
            executingDeadJobChecker.start();
            executableDeadJobChecker.start();
        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
            feedbackJobSendChecker.stop();
            executingDeadJobChecker.stop();
            executableDeadJobChecker.stop();
        }
    }
}
