package com.github.ltsopensource.core.properties;


import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;
import com.github.ltsopensource.core.cluster.AbstractConfigProperties;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.jobtracker")
public class JobTrackerProperties extends AbstractConfigProperties {

    /**
     * 监听端口
     */
    private Integer listenPort;

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
    }
}
