package com.github.ltsopensource.spring.tasktracker;

import com.github.ltsopensource.tasktracker.runner.JobRunner;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

/**
 * @author Robert HG (254963746@qq.com)on 12/21/15.
 */
@Log4j2
public class JobRunnerHolder {

    private static final Map<String, JobRunner> JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static void add(String shardValue, JobRunner jobRunner) {
        JOB_RUNNER_MAP.put(shardValue, jobRunner);
    }

    public static JobRunner getJobRunner(String shardValue) {
        return JOB_RUNNER_MAP.get(shardValue);
    }

    public static void addLTSBean(Object bean) {
        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getMethods();
        if (methods != null && methods.length > 0) {
            for (final Method method : methods) {
                if (method.isAnnotationPresent(JobRunnerItem.class)) {
                    JobRunnerItem jobRunnerItem = method.getAnnotation(JobRunnerItem.class);
                    String shardValue = jobRunnerItem.shardValue();
                    if (StringUtils.isEmpty(shardValue)) {
                        log.error(
                            clazz.getName() + ":" + method.getName() + " " + JobRunnerItem.class.getName() + " shardValue can not be null");
                        continue;
                    }
                    JobRunnerHolder.add(shardValue, JobRunnerBuilder.build(bean, method, method.getParameterTypes()));
                }
            }
        }
    }
}
