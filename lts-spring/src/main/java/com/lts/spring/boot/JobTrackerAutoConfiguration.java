package com.lts.spring.boot;

import com.lts.core.cluster.AbstractJobNode;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.jobtracker.JobTracker;
import com.lts.spring.boot.annotation.EnableJobTracker;
import com.lts.spring.boot.properties.JobTrackerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableJobTracker.class)
@EnableConfigurationProperties(JobTrackerProperties.class)
public class JobTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private JobTrackerProperties properties;
    private JobTracker jobTracker;

    @Override
    protected void initJobNode() {
        jobTracker = new JobTracker();
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
        if(StringUtils.isNotEmpty(properties.getBindIp())){
            jobTracker.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                jobTracker.addConfig(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_TRACKER;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return jobTracker;
    }
}
