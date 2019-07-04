package com.github.ltsopensource.jobclient.support;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.domain.Response;
import java.util.List;

/**
 * 用来处理客户端请求过载问题
 *
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public interface JobSubmitProtector {

    int acquireTimeout = 100;

    public Response execute(final List<Job> jobs, final JobSubmitExecutor<Response> jobSubmitExecutor);
}
