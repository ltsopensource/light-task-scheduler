package com.lts.queue;

import com.lts.admin.request.JobQueueReq;
import com.lts.queue.domain.JobPo;
import com.lts.admin.response.PaginationRsp;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public interface JobQueue {

    PaginationRsp<JobPo> pageSelect(JobQueueReq request);

    boolean selectiveUpdateByJobId(JobQueueReq request);

    boolean selectiveUpdateByTaskId(JobQueueReq request);

}
