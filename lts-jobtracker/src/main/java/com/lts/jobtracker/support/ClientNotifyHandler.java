package com.lts.jobtracker.support;

import com.lts.core.domain.TaskTrackerJobResult;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public interface ClientNotifyHandler<T extends TaskTrackerJobResult> {

    /**
     * 通知成功的处理
     */
    public void handleSuccess(List<T> jobResults);

    /**
     * 通知失败的处理
     */
    public void handleFailed(List<T> jobResults);

}
