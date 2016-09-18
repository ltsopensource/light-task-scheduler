package com.github.ltsopensource.jobclient.support;

import com.github.ltsopensource.core.domain.JobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/29/14.
 */
public interface JobCompletedHandler {

    /**
     * 处理返回结果
     */
    void onComplete(List<JobResult> jobResults);
}
