package com.lts.job.client.support;

import com.lts.job.core.domain.JobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/29/14.
 */
public interface JobFinishedHandler {

    /**
     * 处理返回结果
     * @param jobResults
     */
    public void handle(List<JobResult> jobResults);
}
