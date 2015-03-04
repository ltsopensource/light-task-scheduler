package com.lts.job.core.cluster;

import com.lts.job.core.remoting.RemotingServerDelegate;
import com.lts.job.core.support.Application;
import com.lts.job.remoting.netty.NettyRemotingServer;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import com.lts.job.remoting.netty.NettyServerConfig;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象 netty 服务端
 */
public abstract class AbstractServerNode<T extends Node> extends AbstractJobNode<T> {

    protected RemotingServerDelegate remotingServer;

    protected void nodeStart() {

        NettyServerConfig config = new NettyServerConfig();
        // config 配置
        config.setListenPort(getListenPort());

        remotingServer = new RemotingServerDelegate(new NettyRemotingServer(config));

        remotingServer.start();

        NettyRequestProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {

            remotingServer.registerDefaultProcessor(defaultProcessor,
                    Executors.newCachedThreadPool());
        }
    }

    public void setListenPort(int listenPort) {
        config.setListenPort(listenPort);
    }

    protected void nodeStop() {
        remotingServer.shutdown();
    }

    /**
     * 得到默认的处理器
     *
     * @return
     */
    protected abstract NettyRequestProcessor getDefaultProcessor();

    /**
     * 得到监听接口
     *
     * @return
     */
    protected Integer getListenPort() {
        return Application.Config.getListenPort();
    }

}
