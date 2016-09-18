package com.github.ltsopensource.jobtracker;

import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;
import com.github.ltsopensource.core.cluster.AbstractNodeBuilder;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.properties.JobTrackerProperties;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
public class JobTrackerBuilder extends AbstractNodeBuilder<JobTracker, JobTrackerBuilder> {

    @Override
    protected JobTracker build0() {
        JobTrackerProperties properties = PropertiesConfigurationFactory.createPropertiesConfiguration(JobTrackerProperties.class, locations);
        return buildByProperties(properties);
    }

    public static JobTracker buildByProperties(JobTrackerProperties properties) {

        properties.checkProperties();

        JobTracker jobTracker = new JobTracker();
        jobTracker.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtils.isNotEmpty(properties.getClusterName())) {
            jobTracker.setClusterName(properties.getClusterName());
        }
        if (properties.getListenPort() != null) {
            jobTracker.setListenPort(properties.getListenPort());
        }
        if (StringUtils.isNotEmpty(properties.getIdentity())) {
            jobTracker.setIdentity(properties.getIdentity());
        }
        if (StringUtils.isNotEmpty(properties.getBindIp())) {
            jobTracker.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                jobTracker.addConfig(entry.getKey(), entry.getValue());
            }
        }
        return jobTracker;
    }
}
