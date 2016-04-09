package com.lts.spring.boot;

import com.lts.core.cluster.AbstractJobNode;
import com.lts.core.cluster.NodeType;
import com.lts.core.commons.utils.CollectionUtils;
import com.lts.core.commons.utils.StringUtils;
import com.lts.spring.boot.annotation.EnableTaskTracker;
import com.lts.spring.boot.annotation.JobRunner4TaskTracker;
import com.lts.spring.boot.properties.TaskTrackerProperties;
import com.lts.tasktracker.TaskTracker;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@Configuration
@ConditionalOnBean(annotation = EnableTaskTracker.class)
@EnableConfigurationProperties(TaskTrackerProperties.class)
public class TaskTrackerAutoConfiguration extends AbstractAutoConfiguration {

    @Autowired
    private TaskTrackerProperties properties;
    private TaskTracker taskTracker;

    @Override
    protected void initJobNode() {
        taskTracker = new TaskTracker();
        taskTracker.setRegistryAddress(properties.getRegistryAddress());
        if (StringUtils.isNotEmpty(properties.getClusterName())) {
            taskTracker.setClusterName(properties.getClusterName());
        }
        if (StringUtils.isNotEmpty(properties.getIdentity())) {
            taskTracker.setIdentity(properties.getIdentity());
        }
        if (StringUtils.isNotEmpty(properties.getNodeGroup())) {
            taskTracker.setNodeGroup(properties.getNodeGroup());
        }
        if (StringUtils.isNotEmpty(properties.getDataPath())) {
            taskTracker.setDataPath(properties.getDataPath());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                taskTracker.addConfig(entry.getKey(), entry.getValue());
            }
        }
        if (properties.getWorkThreads() != 0) {
            taskTracker.setWorkThreads(properties.getWorkThreads());
        }

        Map<String, Object> jobRunners = applicationContext.getBeansWithAnnotation(JobRunner4TaskTracker.class);
        if (CollectionUtils.isNotEmpty(jobRunners)) {
            if (jobRunners.size() > 1) {
                throw new IllegalArgumentException("annotation @" + JobRunner4TaskTracker.class.getSimpleName() + " only should have one");
            }
            for (final Map.Entry<String, Object> entry : jobRunners.entrySet()) {
                Object handler = entry.getValue();
                if (handler instanceof JobRunner) {
                    taskTracker.setRunnerFactory(new RunnerFactory() {
                        @Override
                        public JobRunner newRunner() {
                            return (JobRunner) entry.getValue();
                        }
                    });
                } else {
                    throw new IllegalArgumentException(entry.getKey() + "  is not instance of " + JobRunner.class.getName());
                }
            }
        }
    }

    @Override
    protected NodeType nodeType() {
        return NodeType.TASK_TRACKER;
    }

    @Override
    protected AbstractJobNode getJobNode() {
        return taskTracker;
    }
}
