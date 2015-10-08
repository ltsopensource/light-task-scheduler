package com.lts.jobtracker.support;

import com.lts.queue.JobFeedbackQueue;
import com.lts.queue.domain.JobFeedbackPo;

/**
 * 老数据处理handler（像那种JobClient）
 *
 * @author Robert HG (254963746@qq.com) on 3/30/15.
 */
public interface OldDataHandler {

    boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po);

}
