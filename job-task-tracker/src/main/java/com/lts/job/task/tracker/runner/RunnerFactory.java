package com.lts.job.task.tracker.runner;

import com.lts.job.common.AppConfigure;
import com.lts.job.common.support.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/16/14.
 * Job Runner 的工厂类
 */
public class RunnerFactory {

    private static String RUNNER_CLASS = AppConfigure.getString("app.job.runner.class");
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerFactory.class);
    private volatile static Class clazz;

    static{
        try {
            clazz = Application.getAttribute(Application.JOB_RUNNING_CLASS);
            if(clazz == null){
                clazz = Class.forName(RUNNER_CLASS);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("app.job.runner.class is error ! ", e);
        }
    }

    public static JobRunner newRunner() {
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
