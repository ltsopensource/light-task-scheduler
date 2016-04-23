package com.github.ltsopensource.core.cluster;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.remoting.HeartBeatMonitor;
import com.github.ltsopensource.core.remoting.RemotingClientDelegate;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.remoting.RemotingClient;
import com.github.ltsopensource.remoting.RemotingClientConfig;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象客户端
 */
public abstract class AbstractClientNode<T extends Node, Context extends AppContext> extends AbstractJobNode<T, Context> {

    protected RemotingClientDelegate remotingClient;
    private HeartBeatMonitor heartBeatMonitor;

    protected void remotingStart() {
        remotingClient.start();
        heartBeatMonitor.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(ExtConfig.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName(), true)));
        }
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected void remotingStop() {
        heartBeatMonitor.stop();
        remotingClient.shutdown();
    }

    /**
     * 设置节点组名
     */
    public void setNodeGroup(String nodeGroup) {
        config.setNodeGroup(nodeGroup);
    }

    public boolean isServerEnable() {
        return remotingClient.isServerEnable();
    }

    @Override
    protected void beforeRemotingStart() {
        //
        this.remotingClient = new RemotingClientDelegate(getRemotingClient(new RemotingClientConfig()), appContext);
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient, appContext);

        beforeStart();
    }

    private RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingClient(appContext, remotingClientConfig);
    }

    @Override
    protected void afterRemotingStart() {
        // 父类要做的
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

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();

}
