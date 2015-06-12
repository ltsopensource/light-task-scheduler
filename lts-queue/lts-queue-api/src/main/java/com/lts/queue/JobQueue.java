package com.lts.queue;

import com.lts.core.domain.PageResponse;
import com.lts.core.domain.JobQueueRequest;
import com.lts.queue.domain.JobPo;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public interface JobQueue {

    PageResponse<JobPo> pageSelect(JobQueueRequest request);

    boolean selectiveUpdate(JobQueueRequest request);

}
