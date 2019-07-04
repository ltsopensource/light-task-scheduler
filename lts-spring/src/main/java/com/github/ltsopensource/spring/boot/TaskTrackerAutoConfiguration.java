package com.github.ltsopensource.spring.boot;

import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.spring.boot.annotation.EnableTaskTracker;
import com.github.ltsopensource.spring.boot.properties.TaskTrackerProperties;
import com.github.ltsopensource.spring.tasktracker.JobDispatcher;
import com.github.ltsopensource.tasktracker.TaskTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableTaskTracker.class)
@EnableConfigurationProperties(TaskTrackerProperties.class)
public class TaskTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired(required = false)
    private TaskTrackerProperties properties;
    private TaskTracker taskTracker;

    @Override
    protected void initJobNode() {
    }

    String JOB_RUNNER_BEAN_NAME = "LTS_".concat(JobDispatcher.class.getSimpleName());


    @Override
    protected NodeType nodeType() {
        return NodeType.TASK_TRACKER;
    }

}
