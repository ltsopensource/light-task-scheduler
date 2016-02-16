package com.lts.remoting.lts;

import com.lts.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsRemotingTransporter implements RemotingTransporter {
    @Override
    public RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig) {
        return new LtsRemotingServer(remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig) {
        return new LtsRemotingClient(remotingClientConfig);
    }
}
