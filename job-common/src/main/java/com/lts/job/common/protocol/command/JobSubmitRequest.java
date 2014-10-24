package com.lts.job.common.protocol.command;

import com.lts.job.common.domain.Job;
import com.lts.job.remoting.annotation.NotNull;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 * 任务传递信息
 */
public class JobSubmitRequest extends AbstractCommandBody {

    @NotNull
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
