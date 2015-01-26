package com.lts.job.common.cluster;

import com.lts.job.common.remoting.HeartBeatMonitor;
import com.lts.job.common.remoting.RemotingClientDelegate;
import com.lts.job.common.support.Application;
import com.lts.job.common.util.StringUtils;
import com.lts.job.remoting.netty.NettyClientConfig;
import com.lts.job.remoting.netty.NettyRemotingClient;
import com.lts.job.remoting.netty.NettyRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 * 抽象 netty 客户端
 */
public abstract class AbstractClientNode<T extends Node> extends AbstractJobNode<T> {

    protected RemotingClientDelegate remotingClient;
    private HeartBeatMonitor heartBeatMonitor;

    public AbstractClientNode() {
        this.remotingClient = new RemotingClientDelegate(new NettyRemotingClient(getNettyClientConfig()));
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient);
    }

    protected void nodeStart() {
        remotingClient.start();

        NettyRequestProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {

            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newCachedThreadPool());
        }

        // 用于发送心跳检测
        heartBeatMonitor.start();
    }

    /**
     * 得到默认的处理器
     *
     * @return
     */
    protected abstract NettyRequestProcessor getDefaultProcessor();

    protected void nodeStop() {
        remotingClient.shutdown();
        heartBeatMonitor.destroy();
    }

    public void setWorkThreads(int workThreads) {
        config.setWorkThreads(workThreads);
        Application.setAttribute(Application.KEY_AVAILABLE_THREADS, config.getWorkThreads());
    }

    /**
     * 设置节点组名
     * @param nodeGroup
     */
    public void setNodeGroup(String nodeGroup){
        config.setNodeGroup(nodeGroup);
    }

    public void setJobInfoSavePath(String jobInfoSavePath) {
        if(StringUtils.isNotEmpty(jobInfoSavePath)){
            config.setJobInfoSavePath(jobInfoSavePath);
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

}
