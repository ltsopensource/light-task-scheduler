package com.github.ltsopensource.jobtracker.support;

import com.github.ltsopensource.core.domain.JobRunResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public interface ClientNotifyHandler<T extends JobRunResult> {

    /**
     * 通知成功的处理
     */
    void handleSuccess(List<T> jobResults);

    /**
     * 通知失败的处理
     */
    void handleFailed(List<T> jobResults);

}
