package com.github.ltsopensource.remoting.netty;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class NettyRemotingTransporter implements RemotingTransporter {

    @Override
    public RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        return new NettyRemotingServer(appContext, remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig) {
        return new NettyRemotingClient(appContext, remotingClientConfig);
    }
}
