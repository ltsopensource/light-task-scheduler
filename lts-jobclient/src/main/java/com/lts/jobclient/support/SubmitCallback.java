package com.lts.jobclient.support;

import com.lts.remoting.protocol.RemotingCommand;

/**
 * @author Robert HG (254963746@qq.com) on 5/30/15.
 */
public interface SubmitCallback {

    public void call(final RemotingCommand responseCommand);

}
