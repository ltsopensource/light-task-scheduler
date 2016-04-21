package com.github.ltsopensource.core.cluster;

import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
public abstract class AbstractConfigProperties {

    /**
     * 节点标识(可选)
     */
    private String identity;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 执行绑定的本地ip
     */
    private String bindIp;
    /**
     * 额外参数配置
     */
    private Map<String, String> configs = new HashMap<String, String>();

    public abstract void checkProperties() throws ConfigPropertiesIllegalException;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

}
