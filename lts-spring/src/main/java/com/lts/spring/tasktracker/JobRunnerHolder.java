package com.lts.spring.tasktracker;

import com.lts.tasktracker.runner.JobRunner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com)on 12/21/15.
 */
public class JobRunnerHolder {

    private static final Map<String, JobRunner> JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static void add(String shardValue, JobRunner jobRunner) {
        JOB_RUNNER_MAP.put(shardValue, jobRunner);
    }

    public static JobRunner getJobRunner(String shardValue) {
        return JOB_RUNNER_MAP.get(shardValue);
    }
}
