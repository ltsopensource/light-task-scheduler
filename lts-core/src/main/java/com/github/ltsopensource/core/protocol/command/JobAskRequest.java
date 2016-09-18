package com.github.ltsopensource.core.protocol.command;

import com.github.ltsopensource.remoting.exception.RemotingCommandFieldCheckException;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 1993281575847386175L;
	
	List<String> jobIds;

    public List<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<String> jobIds) {
        this.jobIds = jobIds;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {
        if (jobIds == null || jobIds.size() == 0) {
            throw new RemotingCommandFieldCheckException("jobIds could not be empty");
        }
    }
}
