package com.lts.core.cluster;

import com.lts.core.Application;
import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.remoting.netty.NettyRemotingServer;
import com.lts.remoting.netty.NettyRequestProcessor;
import com.lts.remoting.netty.NettyServerConfig;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象 netty 服务端
 */
public abstract class AbstractServerNode<T extends Node, App extends Application> extends AbstractJobNode<T, App> {

    protected RemotingServerDelegate remotingServer;

    protected void remotingStart() {

        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        // config 配置
        nettyServerConfig.setListenPort(config.getListenPort());

        remotingServer = new RemotingServerDelegate(new NettyRemotingServer(nettyServerConfig), application);

        remotingServer.start();

        NettyRequestProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingServer.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize, new NamedThreadFactory(AbstractServerNode.class.getSimpleName())));
        }
    }

    public void setListenPort(int listenPort) {
        config.setListenPort(listenPort);
    }

    protected void remotingStop() {
        remotingServer.shutdown();
    }

    /**
     * 得到默认的处理器
     *
     * @return
     */
    protected abstract NettyRequestProcessor getDefaultProcessor();

}
