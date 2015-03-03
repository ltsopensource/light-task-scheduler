package com.lts.job.tracker.support;

import com.lts.job.common.cluster.Node;
import com.lts.job.common.listener.MasterNodeChangeListener;
import com.lts.job.common.support.Application;
import com.lts.job.common.support.SingletonBeanContext;

/**
 * @author Robert HG (254963746@qq.com) on 8/24/14.
 * JobTracker master 节点变化之后
 */
public class JobTrackerMasterChangeListener implements MasterNodeChangeListener {

    @Override
    public void change(Node master, boolean isMaster) {

        FeedbackJobSendChecker feedbackJobSendChecker = SingletonBeanContext.getBean(FeedbackJobSendChecker.class);

        if (Application.Config.getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
            feedbackJobSendChecker.start();

        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
            feedbackJobSendChecker.stop();
        }
    }
}
