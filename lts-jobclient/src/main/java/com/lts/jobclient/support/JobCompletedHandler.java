package com.lts.jobclient.support;

import com.lts.core.domain.JobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/29/14.
 */
public interface JobCompletedHandler {

    /**
     * 处理返回结果
     */
    public void onComplete(List<JobResult> jobResults);
}
