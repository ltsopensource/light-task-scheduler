package com.github.ltsopensource.tasktracker;

import com.github.ltsopensource.autoconfigure.PropertiesConfigurationFactory;
import com.github.ltsopensource.core.cluster.AbstractNodeBuilder;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.commons.utils.StringUtils;
import com.github.ltsopensource.core.properties.TaskTrackerProperties;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 4/21/16.
 */
public class TaskTrackerBuilder extends AbstractNodeBuilder<TaskTracker, TaskTrackerBuilder> {

    @Override
    protected TaskTracker build0() {

        TaskTrackerProperties properties = PropertiesConfigurationFactory.createPropertiesConfiguration(TaskTrackerProperties.class, locations);
        return buildByProperties(properties);
    }

    @SuppressWarnings("unchecked")
    public static TaskTracker buildByProperties(TaskTrackerProperties properties) {
        TaskTracker taskTracker = new TaskTracker();
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
        if (StringUtils.isNotEmpty(properties.getBindIp())) {
            taskTracker.setBindIp(properties.getBindIp());
        }
        if (CollectionUtils.isNotEmpty(properties.getConfigs())) {
            for (Map.Entry<String, String> entry : properties.getConfigs().entrySet()) {
                taskTracker.addConfig(entry.getKey(), entry.getValue());
            }
        }
        if (properties.getBizLoggerLevel() != null) {
            taskTracker.setBizLoggerLevel(properties.getBizLoggerLevel());
        }
        if (properties.getWorkThreads() != 0) {
            taskTracker.setWorkThreads(properties.getWorkThreads());
        }
        if (properties.getJobRunnerClass() != null) {
            taskTracker.setJobRunnerClass((Class<? extends JobRunner>) properties.getJobRunnerClass());
        }

        return taskTracker;
    }
}
