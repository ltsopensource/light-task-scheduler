package com.lts.queue;

import com.lts.web.request.JobQueueRequest;
import com.lts.queue.domain.JobPo;
import com.lts.web.response.PageResponse;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public interface JobQueue {

    PageResponse<JobPo> pageSelect(JobQueueRequest request);

    boolean selectiveUpdate(JobQueueRequest request);

}
