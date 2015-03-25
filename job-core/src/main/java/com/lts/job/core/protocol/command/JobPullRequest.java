package com.lts.job.core.protocol.command;

/**
 * Job pull request
 * Created by hugui on 3/25/15.
 */
public class JobPullRequest extends AbstractCommandBody {

    private Integer availableThreads;

    public Integer getAvailableThreads() {
        return availableThreads;
    }

    public void setAvailableThreads(Integer availableThreads) {
        this.availableThreads = availableThreads;
    }
}
