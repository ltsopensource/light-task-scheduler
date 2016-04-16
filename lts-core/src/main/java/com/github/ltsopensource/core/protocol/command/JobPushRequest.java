package com.github.ltsopensource.core.protocol.command;

import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.remoting.annotation.NotNull;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class JobPushRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 2986743693237022215L;
	
	@NotNull
    private JobMeta jobMeta;

    public JobMeta getJobMeta() {
        return jobMeta;
    }

    public void setJobMeta(JobMeta jobMeta) {
        this.jobMeta = jobMeta;
    }
}
