package com.github.ltsopensource.remoting;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.spi.SPI;
import com.github.ltsopensource.core.constant.ExtConfig;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI(key = ExtConfig.REMOTING, dftValue = "netty")
public interface RemotingTransporter {

    RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig);

    RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig);

}
