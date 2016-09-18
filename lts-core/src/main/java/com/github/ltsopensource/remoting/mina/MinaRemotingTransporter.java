package com.github.ltsopensource.remoting.mina;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class MinaRemotingTransporter implements RemotingTransporter {
    @Override
    public RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        return new MinaRemotingServer(remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig) {
        return new MinaRemotingClient(remotingClientConfig);
    }
}
