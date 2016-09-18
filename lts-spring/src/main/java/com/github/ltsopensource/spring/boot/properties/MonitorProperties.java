package com.github.ltsopensource.spring.boot.properties;

import com.github.ltsopensource.core.cluster.AbstractConfigProperties;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.monitor")
public class MonitorProperties extends AbstractConfigProperties {

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
    }
}
