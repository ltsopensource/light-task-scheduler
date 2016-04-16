package com.github.ltsopensource.core.protocol.command;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.remoting.annotation.NotNull;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         任务传递信息
 */
public class JobSubmitRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 7229438891247265777L;
	
	@NotNull
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

}
