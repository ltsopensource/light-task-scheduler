package com.github.ltsopensource.core;

import com.github.ltsopensource.cmd.HttpCmdServer;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.core.cluster.MasterElector;
import com.github.ltsopensource.core.cluster.SubscribedNodeManager;
import com.github.ltsopensource.core.monitor.MStatReporter;
import com.github.ltsopensource.core.protocol.command.CommandBodyWrapper;
import com.github.ltsopensource.core.registry.RegistryStatMonitor;
import com.github.ltsopensource.ec.EventCenter;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         用来存储 程序的数据
 */
public abstract class AppContext {

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
    private MStatReporter mStatReporter;
    // 注册中心状态监控
    private RegistryStatMonitor registryStatMonitor;
    // 命令中心
    private HttpCmdServer httpCmdServer;

    public MStatReporter getMStatReporter() {
        return mStatReporter;
    }

    public void setMStatReporter(MStatReporter mStatReporter) {
        this.mStatReporter = mStatReporter;
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

    public HttpCmdServer getHttpCmdServer() {
        return httpCmdServer;
    }

    public void setHttpCmdServer(HttpCmdServer httpCmdServer) {
        this.httpCmdServer = httpCmdServer;
    }
}
