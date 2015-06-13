package com.lts.jobclient.support;

import com.lts.core.domain.JobResult;
import com.lts.core.domain.TaskTrackerJobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/29/14.
 */
public interface JobFinishedHandler {

    /**
     * 处理返回结果
     */
    public void handle(List<JobResult> jobResults);
}
