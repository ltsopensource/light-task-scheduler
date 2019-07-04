package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.spring.boot.annotation.EnableJobTracker;
import com.github.ltsopensource.spring.boot.properties.JobTrackerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.JOB_TRACKER;
    }

}
