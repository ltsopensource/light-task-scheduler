package com.lts.core.cluster;

import com.lts.core.Application;
import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.remoting.RemotingServerDelegate;
import com.lts.core.spi.ServiceLoader;
import com.lts.remoting.RemotingProcessor;
import com.lts.remoting.RemotingServer;
import com.lts.remoting.RemotingServerConfig;
import com.lts.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象服务端
 */
public abstract class AbstractServerNode<T extends Node, App extends Application> extends AbstractJobNode<T, App> {

    protected RemotingServerDelegate remotingServer;

    protected void remotingStart() {

        remotingServer.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
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

    @Override
    protected void beforeRemotingStart() {
        RemotingServerConfig remotingServerConfig = new RemotingServerConfig();
        // config 配置
        if (config.getListenPort() == 0) {
            config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
            node.setPort(config.getListenPort());
        }
        remotingServerConfig.setListenPort(config.getListenPort());

        remotingServer = new RemotingServerDelegate(getRemotingServer(remotingServerConfig), application);

        beforeStart();
    }

    private RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingServer(remotingServerConfig);
    }

    @Override
    protected void afterRemotingStart() {
        afterStart();
    }

    @Override
    protected void beforeRemotingStop() {
        beforeStop();
    }

    @Override
    protected void afterRemotingStop() {
        afterStop();
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();


}
