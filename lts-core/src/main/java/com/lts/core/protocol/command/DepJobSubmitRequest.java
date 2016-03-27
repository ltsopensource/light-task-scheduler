package com.lts.core.protocol.command;

import com.lts.core.domain.DepJobGroup;
import com.lts.remoting.annotation.NotNull;

/**
 * Created by hugui.hg on 3/27/16.
 */
public class DepJobSubmitRequest extends AbstractRemotingCommandBody {

    @NotNull
    private DepJobGroup jobGroup;

    public DepJobGroup getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(DepJobGroup jobGroup) {
        this.jobGroup = jobGroup;
    }
}
