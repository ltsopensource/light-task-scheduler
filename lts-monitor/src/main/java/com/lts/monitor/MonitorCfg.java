package com.lts.monitor;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class MonitorCfg {

    private String registryAddress;

    private String bindIp;

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
