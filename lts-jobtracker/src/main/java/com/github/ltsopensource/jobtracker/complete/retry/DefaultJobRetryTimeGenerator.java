package com.github.ltsopensource.jobtracker.complete.retry;

import com.github.ltsopensource.core.support.SystemClock;

/**
 * @author Robert HG (254963746@qq.com) on 6/28/16.
 */
public class DefaultJobRetryTimeGenerator implements JobRetryTimeGenerator {

    @Override
    public long getNextRetryTriggerTime(int retryTimes, int retryInterval) {
        return SystemClock.now() + retryInterval;
    }
}
