package com.github.ltsopensource.startup.tasktracker;

import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.tasktracker.TaskTracker;

import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 9/11/15.
 */
public class DefaultStartup {

    @SuppressWarnings("unchecked")
	public static TaskTracker start(TaskTrackerCfg cfg) {

        final TaskTracker taskTracker = new TaskTracker();
        taskTracker.setJobRunnerClass(cfg.getJobRunnerClass());
        taskTracker.setRegistryAddress(cfg.getRegistryAddress());
        taskTracker.setNodeGroup(cfg.getNodeGroup());
        taskTracker.setClusterName(cfg.getClusterName());
        taskTracker.setWorkThreads(cfg.getWorkThreads());
        taskTracker.setDataPath(cfg.getDataPath());
        // 业务日志级别
        if (cfg.getBizLoggerLevel() == null) {
            taskTracker.setBizLoggerLevel(Level.INFO);
        } else {
            taskTracker.setBizLoggerLevel(cfg.getBizLoggerLevel());
        }

        for (Map.Entry<String, String> config : cfg.getConfigs().entrySet()) {
            taskTracker.addConfig(config.getKey(), config.getValue());
        }

        return taskTracker;
    }

}
