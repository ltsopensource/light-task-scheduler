package com.github.ltsopensource.jobclient.support;

import com.github.ltsopensource.core.domain.Job;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/21/15.
 */
public interface JobSubmitExecutor<T> {

    T execute(List<Job> jobs);
}
