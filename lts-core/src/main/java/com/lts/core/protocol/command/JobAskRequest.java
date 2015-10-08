package com.lts.core.protocol.command;

import com.lts.remoting.exception.RemotingCommandFieldCheckException;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskRequest extends AbstractCommandBody {

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
            throw new RemotingCommandFieldCheckException("jobIds 不能为空!");
        }
    }
}
