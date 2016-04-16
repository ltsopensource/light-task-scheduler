package com.github.ltsopensource.jobtracker.support.policy;

import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.jobtracker.support.OldDataHandler;
import com.github.ltsopensource.queue.JobFeedbackQueue;
import com.github.ltsopensource.queue.domain.JobFeedbackPo;

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
            jobFeedbackQueue.remove(po.getJobRunResult().getJobMeta().getJob().getTaskTrackerNodeGroup(), po.getId());
            return true;
        }

        return false;
    }
}
