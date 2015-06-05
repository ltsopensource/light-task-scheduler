package com.lts.job.task.tracker.runner;

import com.lts.job.core.domain.Job;
import com.lts.job.task.tracker.domain.Response;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 */
public interface RunnerCallback {

    /**
     * 执行完成, 可能是成功, 也可能是失败
     * @param response
     * @return 如果有新的任务, 那么返回新的任务过来
     */
    public Job runComplete(Response response);

}
