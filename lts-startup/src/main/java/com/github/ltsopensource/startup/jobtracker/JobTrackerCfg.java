package com.github.ltsopensource.startup.jobtracker;

import java.util.Map;

/**
 * JobTracker 配置文件
 * @author Robert HG (254963746@qq.com) on 9/1/15.
 */
public class JobTrackerCfg {

    private String registryAddress;

    private String bindIp;

    private int listenPort;

    private String clusterName;

    private Map<String, String> configs;

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
