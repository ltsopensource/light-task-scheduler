package com.github.ltsopensource.jobtracker.complete;

import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.core.domain.JobRunResult;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/11/15.
 */
public interface JobFinishHandler {

    void onComplete(List<JobRunResult> results);

    void finishCronJob(String jobId);

    void finishNoReplyPrevCronJob(JobMeta jobMeta);

    void finishNoReplyPrevRepeatJob(JobMeta jobMeta, boolean isRetryForThisTime);

    void finishRepeatJob(String jobId, boolean isRetryForThisTime);
}
