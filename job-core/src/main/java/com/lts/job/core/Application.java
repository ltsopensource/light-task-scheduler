package com.lts.job.core;

import com.lts.job.core.cluster.MasterElector;
import com.lts.job.core.cluster.SubscribedNodeManager;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.ec.EventCenter;
import com.lts.job.core.protocol.command.CommandBodyWrapper;
import com.lts.job.core.registry.PathParser;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 *         用来存储 程序的数据
 */
public abstract class Application {

    public Application() {
        this.commandBodyWrapper = new CommandBodyWrapper(this);
        this.subscribedNodeManager = new SubscribedNodeManager(this);
        this.masterElector = new MasterElector(this);
    }

    // 节点配置信息
    private JobNodeConfig config;
    // 节点管理
    private SubscribedNodeManager subscribedNodeManager;
    // master节点选举者
    private MasterElector masterElector;
    // 节点通信CommandBody包装器
    private CommandBodyWrapper commandBodyWrapper;
    // 节点路径解析器
    private PathParser pathParser;
    // 事件中心
    private EventCenter eventCenter;

    public PathParser getPathParser() {
        return pathParser;
    }

    public void setPathParser(PathParser pathParser) {
        this.pathParser = pathParser;
    }

    public CommandBodyWrapper getCommandBodyWrapper() {
        return commandBodyWrapper;
    }

    public void setCommandBodyWrapper(CommandBodyWrapper commandBodyWrapper) {
        this.commandBodyWrapper = commandBodyWrapper;
    }

    public JobNodeConfig getConfig() {
        return config;
    }

    public void setConfig(JobNodeConfig config) {
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

    public EventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

}
