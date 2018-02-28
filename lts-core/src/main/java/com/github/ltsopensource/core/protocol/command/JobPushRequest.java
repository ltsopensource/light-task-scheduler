package com.github.ltsopensource.core.protocol.command;

import com.github.ltsopensource.core.domain.JobMeta;
import com.github.ltsopensource.remoting.annotation.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class JobPushRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 2986743693237022215L;
	
	@NotNull
    private List<JobMeta> jobMetaList;

    /**
     *  jobClient(lts-1.7.0) deserialize  message from jobTracker (lts-1.6.9)
     */
	@Deprecated
    public void setJobMeta(JobMeta jobMeta) {
        this.jobMetaList = Arrays.asList(jobMeta);
    }

    public List<JobMeta> getJobMetaList() {
        return jobMetaList;
    }

    public void setJobMetaList(List<JobMeta> jobMetaList) {
        this.jobMetaList = jobMetaList;
    }
}
