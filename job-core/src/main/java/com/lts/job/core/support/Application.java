package com.lts.job.core.support;

import com.lts.job.core.cluster.MasterElector;
import com.lts.job.core.cluster.NodeManager;
import com.lts.job.core.domain.JobNodeConfig;
import com.lts.job.core.protocol.command.CommandWrapper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 * 用来存储 程序的数据
 */
public class Application {

    private final ConcurrentHashMap<String, Object> keyValue = new ConcurrentHashMap<String, Object>();

    public void setAttribute(String key, Object value) {
        keyValue.put(key, value);
    }

    public <T> T getAttribute(String key) {
        Object object = keyValue.get(key);
        if (object == null) {
            return null;
        }

        return (T) object;
    }

    private JobNodeConfig Config;
    private NodeManager nodeManager;
    private MasterElector masterElector;
    private CommandWrapper commandWrapper;

    public CommandWrapper getCommandWrapper() {
        return commandWrapper;
    }

    public void setCommandWrapper(CommandWrapper commandWrapper) {
        this.commandWrapper = commandWrapper;
    }

    public JobNodeConfig getConfig() {
        return Config;
    }

    public void setConfig(JobNodeConfig config) {
        Config = config;
    }

    public NodeManager getNodeManager() {
        return nodeManager;
    }

    public void setNodeManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public MasterElector getMasterElector() {
        return masterElector;
    }

    public void setMasterElector(MasterElector masterElector) {
        this.masterElector = masterElector;
    }
}
