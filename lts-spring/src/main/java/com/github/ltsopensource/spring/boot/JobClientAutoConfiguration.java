package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.AbstractJobNode;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.jobclient.support.JobCompletedHandler;
import com.github.ltsopensource.spring.boot.annotation.EnableJobClient;
import com.github.ltsopensource.spring.boot.annotation.JobCompletedHandler4JobClient;
import com.github.ltsopensource.spring.boot.properties.JobClientProperties;
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

    @Autowired(required = false)
    private JobClientProperties properties;
    private JobClient jobClient;

    @Bean
    public JobClient jobClient() {
        return jobClient;
    }

    @Override
    protected void initJobNode() {
        jobClient = JobClientBuilder.buildByProperties(properties);

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
