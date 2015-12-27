package com.lts.core.cluster;

import com.lts.core.Application;
import com.lts.core.constant.Constants;
import com.lts.core.factory.NamedThreadFactory;
import com.lts.core.remoting.HeartBeatMonitor;
import com.lts.core.remoting.RemotingClientDelegate;
import com.lts.core.spi.ServiceLoader;
import com.lts.remoting.RemotingClient;
import com.lts.remoting.RemotingClientConfig;
import com.lts.remoting.RemotingProcessor;
import com.lts.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象客户端
 */
public abstract class AbstractClientNode<T extends Node, App extends Application> extends AbstractJobNode<T, App> {

    protected RemotingClientDelegate remotingClient;
    private HeartBeatMonitor heartBeatMonitor;

    protected void remotingStart() {
        remotingClient.start();
        heartBeatMonitor.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName())));
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

    /**
     * 设置连接JobTracker的负载均衡算法
     *
     * @param loadBalance 算法 random, consistenthash
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }


    @Override
    protected void beforeRemotingStart() {
        //
        this.remotingClient = new RemotingClientDelegate(getRemotingClient(new RemotingClientConfig()), application);
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient, application);

        beforeStart();
    }

    private RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingClient(remotingClientConfig);
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
