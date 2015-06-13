package com.lts.jobtracker.support.policy;

import com.lts.core.support.SystemClock;
import com.lts.queue.JobFeedbackQueue;
import com.lts.queue.domain.JobFeedbackPo;
import com.lts.jobtracker.support.OldDataHandler;

/**
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public class OldDataDeletePolicy implements OldDataHandler {

    private long expired = 30 * 24 * 60 * 60 * 1000L;        // 默认30 天

    public OldDataDeletePolicy() {
    }

    public OldDataDeletePolicy(long expired) {
        this.expired = expired;
    }

    public boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po) {

        if (SystemClock.now() - jobFeedbackPo.getGmtCreated() > expired) {
            // delete
            jobFeedbackQueue.remove(po.getTaskTrackerJobResult().getJobWrapper().getJob().getTaskTrackerNodeGroup(), po.getId());
            return true;
        }

        return false;
    }
}
