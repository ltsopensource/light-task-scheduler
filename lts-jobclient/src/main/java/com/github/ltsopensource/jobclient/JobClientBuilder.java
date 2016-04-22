package com.github.ltsopensource.jobclient;

import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;
import com.github.ltsopensource.core.cluster.AbstractNodeBuilder;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.properties.JobClientProperties;
import com.github.ltsopensource.jobclient.support.JobCompletedHandler;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
public class JobClientBuilder extends AbstractNodeBuilder<JobClient, JobClientBuilder> {

    private JobCompletedHandler jobCompletedHandler;

    public JobClientBuilder setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
        this.jobCompletedHandler = jobCompletedHandler;
        return this;
    }

    @Override
    protected JobClient build0() {
        JobClientProperties properties = PropertiesConfigurationFactory
                .createPropertiesConfiguration(JobClientProperties.class, locations);

        JobClient jobClient = buildByProperties(properties);

        if (jobCompletedHandler != null) {
            jobClient.setJobCompletedHandler(jobCompletedHandler);
        }

        return jobClient;
    }

    public static JobClient buildByProperties(JobClientProperties properties) {
        properties.checkProperties();

        JobClient jobClient;
        if (properties.isUseRetryClient()) {
            jobClient = new RetryJobClient();
        } else {
            jobClient = new JobClient();
        }
        jobClient.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtils.isNotEmpty(properties.getClusterName())) {
            jobClient.setClusterName(properties.getClusterName());
        }
        if (StringUtils.isNotEmpty(properties.getIdentity())) {
            jobClient.setIdentity(properties.getIdentity());
        }
        if (StringUtils.isNotEmpty(properties.getNodeGroup())) {
            jobClient.setNodeGroup(properties.getNodeGroup());
        }
        if (StringUtils.isNotEmpty(properties.getDataPath())) {
            jobClient.setDataPath(properties.getDataPath());
        }
        if (StringUtils.isNotEmpty(properties.getBindIp())) {
            jobClient.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                jobClient.addConfig(entry.getKey(), entry.getValue());
            }
        }
        return jobClient;
    }
}
