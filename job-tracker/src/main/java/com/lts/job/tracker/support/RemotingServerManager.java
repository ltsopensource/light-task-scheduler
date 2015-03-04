package com.lts.job.tracker.support;

import com.lts.job.core.remoting.RemotingServerDelegate;

/**
 * @author Robert HG (254963746@qq.com) on 8/21/14.
 */
public class RemotingServerManager {

    private static RemotingServerDelegate remotingServer;

    public static RemotingServerDelegate getRemotingServer() {
        return RemotingServerManager.remotingServer;
    }

    public static void setRemotingServer(RemotingServerDelegate remotingServer) {
        RemotingServerManager.remotingServer = remotingServer;
    }
}
