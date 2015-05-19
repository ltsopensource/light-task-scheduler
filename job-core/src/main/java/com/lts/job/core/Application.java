package com.lts.job.core;

import com.lts.job.core.cluster.Config;
import com.lts.job.core.cluster.MasterElector;
import com.lts.job.core.cluster.SubscribedNodeManager;
import com.lts.job.core.protocol.command.CommandBodyWrapper;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         用来存储 程序的数据
 */
public abstract class Application {

    // 节点配置信息
    private Config config;
    // 节点管理
    private SubscribedNodeManager subscribedNodeManager;
    // master节点选举者
    private MasterElector masterElector;
    // 节点通信CommandBody包装器
    private CommandBodyWrapper commandBodyWrapper;

    public CommandBodyWrapper getCommandBodyWrapper() {
        return commandBodyWrapper;
    }

    public void setCommandBodyWrapper(CommandBodyWrapper commandBodyWrapper) {
        this.commandBodyWrapper = commandBodyWrapper;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public SubscribedNodeManager getSubscribedNodeManager() {
        return subscribedNodeManager;
    }

    public void setSubscribedNodeManager(SubscribedNodeManager subscribedNodeManager) {
        this.subscribedNodeManager = subscribedNodeManager;
    }

    public MasterElector getMasterElector() {
        return masterElector;
    }

    public void setMasterElector(MasterElector masterElector) {
        this.masterElector = masterElector;
    }

}
