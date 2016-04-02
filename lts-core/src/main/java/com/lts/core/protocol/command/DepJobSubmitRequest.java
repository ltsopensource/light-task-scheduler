package com.lts.core.protocol.command;

import com.lts.core.domain.DependencyJobGroup;
import com.lts.remoting.annotation.NotNull;

/**
 * @author Robert HG (254963746@qq.com) 3/27/16.
 */
public class DepJobSubmitRequest extends AbstractRemotingCommandBody {

    @NotNull
    private DependencyJobGroup jobGroup;

    public DependencyJobGroup getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(DependencyJobGroup jobGroup) {
        this.jobGroup = jobGroup;
    }
}
