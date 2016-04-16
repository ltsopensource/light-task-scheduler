package com.github.ltsopensource.queue;

import com.github.ltsopensource.admin.request.JobQueueReq;
import com.github.ltsopensource.queue.domain.JobPo;
import com.github.ltsopensource.admin.response.PaginationRsp;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public interface JobQueue {

    PaginationRsp<JobPo> pageSelect(JobQueueReq request);

    boolean selectiveUpdateByJobId(JobQueueReq request);

    boolean selectiveUpdateByTaskId(JobQueueReq request);

}
