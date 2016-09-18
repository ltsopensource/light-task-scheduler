package com.github.ltsopensource.core.properties;


import com.github.ltsopensource.autoconfigure.annotation.ConfigurationProperties;
import com.github.ltsopensource.core.cluster.AbstractConfigProperties;
import com.github.ltsopensource.core.commons.utils.Assert;
import com.github.ltsopensource.core.exception.ConfigPropertiesIllegalException;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.jobclient")
public class JobClientProperties extends AbstractConfigProperties {

    private String nodeGroup;
    private boolean useRetryClient = true;
    private String dataPath;

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public boolean isUseRetryClient() {
        return useRetryClient;
    }

    public void setUseRetryClient(boolean useRetryClient) {
        this.useRetryClient = useRetryClient;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    public void checkProperties() throws ConfigPropertiesIllegalException {
        Assert.hasText(getClusterName(), "clusterName must have value.");
        Assert.hasText(getNodeGroup(), "nodeGroup must have value.");
        Assert.hasText(getRegistryAddress(), "registryAddress must have value.");
    }
}
