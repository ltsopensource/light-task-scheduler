package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.tasktracker.domain.TaskTrackerAppContext;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class DefaultRunnerFactory implements RunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerFactory.class);
    private TaskTrackerAppContext appContext;

    public DefaultRunnerFactory(TaskTrackerAppContext appContext) {
        this.appContext = appContext;
    }

    public JobRunner newRunner() {
        try {
            return (JobRunner) appContext.getJobRunnerClass().newInstance();
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
