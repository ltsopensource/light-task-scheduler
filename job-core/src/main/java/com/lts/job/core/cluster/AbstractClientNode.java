package com.lts.job.core.cluster;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.loadbalance.ConsistentHashLoadBalance;
import com.lts.job.core.loadbalance.LoadBalance;
import com.lts.job.core.remoting.HeartBeatMonitor;
import com.lts.job.core.remoting.RemotingClientDelegate;
import com.lts.job.core.util.StringUtils;
import com.lts.job.remoting.netty.NettyClientConfig;
import com.lts.job.remoting.netty.NettyRemotingClient;
import com.lts.job.remoting.netty.NettyRequestProcessor;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象 netty 客户端
 */
public abstract class AbstractClientNode<T extends Node> extends AbstractJobNode<T> {

    protected RemotingClientDelegate remotingClient;
    private LoadBalance loadBalance;
    private HeartBeatMonitor heartBeatMonitor;

    public AbstractClientNode() {

    }

    protected void innerStart() {

        if (loadBalance == null) {
            loadBalance = new ConsistentHashLoadBalance();
        }
        this.remotingClient = new RemotingClientDelegate(
                new NettyRemotingClient(getNettyClientConfig()), application, loadBalance);
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient);

        remotingClient.start();
        heartBeatMonitor.start();

        NettyRequestProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {

            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newCachedThreadPool());
        }
    }

    /**
     * 得到默认的处理器
     *
     * @return
     */
    protected abstract NettyRequestProcessor getDefaultProcessor();

    protected void innerStop() {
        heartBeatMonitor.start();
        remotingClient.shutdown();
    }

    public void setWorkThreads(int workThreads) {
        config.setWorkThreads(workThreads);
        application.setAttribute(Constants.KEY_AVAILABLE_THREADS, config.getWorkThreads());
    }

    /**
     * 设置节点组名
     *
     * @param nodeGroup
     */
    public void setNodeGroup(String nodeGroup) {
        config.setNodeGroup(nodeGroup);
    }

    public void setJobInfoSavePath(String jobInfoSavePath) {
        if (StringUtils.isNotEmpty(jobInfoSavePath)) {
            config.setJobInfoSavePath(jobInfoSavePath + "/.job");
        }
    }

    public boolean isServerEnable() {
        return remotingClient.isServerEnable();
    }

    /**
     * 这个子类可以覆盖
     *
     * @return
     */
    protected NettyClientConfig getNettyClientConfig() {
        NettyClientConfig config = new NettyClientConfig();
        return config;
    }

    /**
     * 设置连接JobTracker的负载均衡算法
     * @param loadBalance
     */
    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

}
