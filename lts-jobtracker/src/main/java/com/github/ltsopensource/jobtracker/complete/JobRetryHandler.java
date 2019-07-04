package com.github.ltsopensource.jobtracker.complete;

import com.github.ltsopensource.core.domain.JobRunResult;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobRetryHandler {

    int retryInterval = 30 * 1000;     // 默认30s

    public void onComplete(List<JobRunResult> results);
}
