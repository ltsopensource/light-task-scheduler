package com.lts.job.core.protocol.command;

import com.lts.job.core.domain.Job;
import com.lts.job.remoting.annotation.NotNull;

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
