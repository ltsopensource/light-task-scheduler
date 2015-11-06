package com.lts.remoting;

import com.lts.core.cluster.Config;
import com.lts.core.extension.Adaptive;
import com.lts.core.extension.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI("netty")
public interface RemotingTransporter {

    @Adaptive("lts.remoting")
    RemotingServer getRemotingServer(Config config, RemotingServerConfig remotingServerConfig);

    @Adaptive("lts.remoting")
    RemotingClient getRemotingClient(Config config, RemotingClientConfig remotingClientConfig);

}
