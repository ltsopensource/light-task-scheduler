package com.github.ltsopensource.core.protocol.command;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/1/16.
 */
public class JobPushResponse extends AbstractRemotingCommandBody {

    private List<String> failedJobIds;

    public List<String> getFailedJobIds() {
        return failedJobIds;
    }

    public void setFailedJobIds(List<String> failedJobIds) {
        this.failedJobIds = failedJobIds;
    }
}
