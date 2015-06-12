package com.lts.core.protocol.command;

import com.lts.core.domain.Job;
import com.lts.remoting.annotation.NotNull;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class JobPushRequest extends AbstractCommandBody{

    @NotNull
    private Job job;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
