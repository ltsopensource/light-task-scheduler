package com.lts.job.tracker.support.listener;

import com.lts.job.core.cluster.Node;
import com.lts.job.core.listener.MasterNodeChangeListener;
import com.lts.job.tracker.domain.JobTrackerApplication;
import com.lts.job.tracker.support.checker.DeadJobChecker;
import com.lts.job.tracker.support.checker.FeedbackJobSendChecker;

/**
 * @author Robert HG (254963746@qq.com) on 8/24/14.
 *         JobTracker master 节点变化之后
 */
public class JobTrackerMasterChangeListener implements MasterNodeChangeListener {

    private JobTrackerApplication application;
    private DeadJobChecker deadJobChecker;
    private FeedbackJobSendChecker feedbackJobSendChecker;

    public JobTrackerMasterChangeListener(JobTrackerApplication application) {
        this.application = application;
        this.deadJobChecker = new DeadJobChecker(application);
        this.application.setDeadJobChecker(deadJobChecker);
        this.feedbackJobSendChecker = new FeedbackJobSendChecker(application);
    }

    @Override
    public void change(Node master, boolean isMaster) {

        if (application.getConfig().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
            feedbackJobSendChecker.start();
            deadJobChecker.start();
        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
            feedbackJobSendChecker.stop();
            deadJobChecker.stop();
        }
    }
}
