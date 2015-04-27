package com.lts.job.tracker.support;

import com.lts.job.tracker.queue.JobFeedbackPo;
import com.lts.job.tracker.queue.JobFeedbackQueue;

/**
 * 老数据处理handler（像那种JobClient）
 *
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public interface OldDataHandler {

    boolean handleJobFeedbackPo(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po);

}
