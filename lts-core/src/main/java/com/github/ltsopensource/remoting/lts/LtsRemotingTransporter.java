package com.github.ltsopensource.remoting.lts;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsRemotingTransporter implements RemotingTransporter {
    @Override
    public RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        return new LtsRemotingServer(remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig) {
        return new LtsRemotingClient(remotingClientConfig);
    }
}
