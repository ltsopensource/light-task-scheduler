package com.lts.job.tracker.support;

import com.lts.job.core.domain.JobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public interface ClientNotifyHandler {

    /**
     * 通知成功的处理
     *
     * @param jobResults
     */
    public void handleSuccess(List<JobResult> jobResults);

    /**
     * 通知失败的处理
     *
     * @param jobResults
     */
    public void handleFailed(List<JobResult> jobResults);

}
