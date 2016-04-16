package com.github.ltsopensource.spring.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@ConfigurationProperties(prefix = "lts.jobclient")
public class JobClientProperties extends AbstractProperties {

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
}
