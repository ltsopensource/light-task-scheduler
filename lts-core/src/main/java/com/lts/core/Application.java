package com.lts.core;

import com.lts.command.CommandCenter;
import com.lts.core.cluster.Config;
import com.lts.core.cluster.MasterElector;
import com.lts.core.cluster.SubscribedNodeManager;
import com.lts.core.monitor.Monitor;
import com.lts.core.protocol.command.CommandBodyWrapper;
import com.lts.core.registry.RegistryStatMonitor;
import com.lts.ec.EventCenter;

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
    // 事件中心
    private EventCenter eventCenter;
    // 监控中心
    private Monitor monitor;
    // 注册中心状态监控
    private RegistryStatMonitor registryStatMonitor;
    // 命令中心
    private CommandCenter commandCenter;

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public EventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

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

    public RegistryStatMonitor getRegistryStatMonitor() {
        return registryStatMonitor;
    }

    public void setRegistryStatMonitor(RegistryStatMonitor registryStatMonitor) {
        this.registryStatMonitor = registryStatMonitor;
    }

    public CommandCenter getCommandCenter() {
        return commandCenter;
    }

    public void setCommandCenter(CommandCenter commandCenter) {
        this.commandCenter = commandCenter;
    }
}
