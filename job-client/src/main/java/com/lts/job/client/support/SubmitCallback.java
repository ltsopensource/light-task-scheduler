package com.lts.job.client.support;

import com.lts.job.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public interface SubmitCallback {

    public void call(final RemotingCommand responseCommand);

}
