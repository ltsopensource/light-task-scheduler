package com.lts.remoting;

import com.lts.core.spi.SPI;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI(key = "lts.remoting", dftValue = "netty")
public interface RemotingTransporter {

    RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig);

    RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig);

}
