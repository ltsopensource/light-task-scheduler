package com.lts.job.task.tracker.runner;

import com.lts.job.task.tracker.domain.TaskTrackerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 3/6/15.
 */
public class DefaultRunnerFactory implements RunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerFactory.class);
    private Class clazz;

    public DefaultRunnerFactory(TaskTrackerApplication application) {
        clazz = application.getJobRunnerClass();
    }

    public JobRunner newRunner() {
        try {
            return (JobRunner) clazz.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
