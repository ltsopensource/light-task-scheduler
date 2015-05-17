package com.lts.job.core.domain;

import com.lts.job.core.cluster.NodeType;
import com.lts.job.core.util.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 8/20/14.
 *         任务节点配置
 */
public class JobNodeConfig {

    // 节点是否可用
    private boolean available = true;
    // 应用节点组
    private String nodeGroup;
    // 唯一标识
    private String identity;
    // 工作线程
    private int workThreads;
    // 节点类型
    private NodeType nodeType;
    // 注册中心 地址
    private String registryAddress;
    // 远程连接超时时间
    private int invokeTimeoutMillis;
    // 监听端口
    private int listenPort;
    // 任务信息存储路径(譬如TaskTracker反馈任务信息给JobTracker, JobTracker down掉了, 那么存储下来等待JobTracker可用时再发送)
    private String jobInfoSavePath;
    // 集群名字
    private String clusterName;

    private final Map<String, Object> parameters = new HashMap<String, Object>();

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getInvokeTimeoutMillis() {
        return invokeTimeoutMillis;
    }

    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        this.invokeTimeoutMillis = invokeTimeoutMillis;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getJobInfoSavePath() {
        return jobInfoSavePath;
    }

    public void setJobInfoSavePath(String jobInfoSavePath) {
        this.jobInfoSavePath = jobInfoSavePath + "/.lts";
    }

    public String getFilePath() {
        return jobInfoSavePath + "/" + nodeType + "/" + nodeGroup + "/";
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public <T> T getParameter(String key) {
        return (T)parameters.get(key);
    }

    public <T> T getParameter(String key, T defaultValue) {
        Object value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T)value;
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }
}
