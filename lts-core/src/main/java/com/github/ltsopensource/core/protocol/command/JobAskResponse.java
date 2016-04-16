package com.github.ltsopensource.core.protocol.command;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class JobAskResponse extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 6614340681500484560L;
	/**
     * 返回不在执行中的jobIds(死掉的)
     */
    List<String> jobIds;

    public List<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<String> jobIds) {
        this.jobIds = jobIds;
    }
}
