package com.lts.spring.boot;

import com.lts.core.cluster.AbstractJobNode;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.jobclient.JobClient;
import com.lts.jobclient.RetryJobClient;
import com.lts.jobclient.support.JobCompletedHandler;
import com.lts.spring.boot.annotation.EnableJobClient;
import com.lts.spring.boot.annotation.JobCompletedHandler4JobClient;
import com.lts.spring.boot.properties.JobClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableJobClient.class)
@EnableConfigurationProperties(JobClientProperties.class)
public class JobClientAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired
    private JobClientProperties properties;
    private JobClient jobClient;

    @Bean
    public JobClient jobClient() {
        return jobClient;
    }

    @Override
    protected void initJobNode() {
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
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                jobClient.addConfig(entry.getKey(), entry.getValue());
            }
        }

        Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(JobCompletedHandler4JobClient.class);
        if (CollectionUtils.isNotEmpty(handlers)) {
            if (handlers.size() > 1) {
                throw new IllegalArgumentException("annotation @" + JobCompletedHandler4JobClient.class.getSimpleName() + " only should have one");
            }
            for (Map.Entry<String, Object> entry : handlers.entrySet()) {
                Object handler = entry.getValue();
                if (handler instanceof JobCompletedHandler) {
                    jobClient.setJobCompletedHandler((JobCompletedHandler) entry.getValue());
                } else {
                    throw new IllegalArgumentException(entry.getKey() + "  is not instance of " + JobCompletedHandler.class.getName());
                }
            }
        }
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_CLIENT;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return jobClient;
    }
}
