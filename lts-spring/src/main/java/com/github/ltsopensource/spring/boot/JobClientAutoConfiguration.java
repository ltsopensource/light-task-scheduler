package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.spring.boot.annotation.EnableJobClient;
import com.github.ltsopensource.spring.boot.properties.JobClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableJobClient.class)
@EnableConfigurationProperties(JobClientProperties.class)
public class JobClientAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private JobClientProperties properties;
    private JobClient jobClient;

    @Bean
    public JobClient jobClient() {
        return jobClient;
    }

    @Override
    protected void initJobNode() {
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_CLIENT;
    }

}
