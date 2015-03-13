package com.lts.job.task.tracker.runner;

import com.lts.job.core.constant.Constants;
import com.lts.job.core.support.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Robert HG (254963746@qq.com) on 3/6/15.
 */
public class DefaultRunnerFactory implements RunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerFactory.class);
    private Class clazz;

    public DefaultRunnerFactory(Application application) {
        clazz = application.getAttribute(Constants.JOB_RUNNING_CLASS);
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
