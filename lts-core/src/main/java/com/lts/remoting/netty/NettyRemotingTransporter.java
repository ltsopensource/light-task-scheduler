package com.lts.remoting.netty;

import com.lts.core.cluster.Config;
import com.lts.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
public class NettyRemotingTransporter implements RemotingTransporter{

    @Override
    public RemotingServer getRemotingServer(Config config, RemotingServerConfig remotingServerConfig) {
        return new NettyRemotingServer(remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(Config config, RemotingClientConfig remotingClientConfig) {
        return new NettyRemotingClient(remotingClientConfig);
    }
}
