package com.lts.example.support;

import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 总入口，在 taskTracker.setJobRunnerClass(JobRunnerDispatcher.class)
 * JobClient 提交 任务时指定 Job 类型  job.setParam("type", "aType")
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class JobRunnerDispatcher implements JobRunner {

    private static final ConcurrentHashMap<String/*type*/, JobRunner>
            JOB_RUNNER_MAP = new ConcurrentHashMap<String, JobRunner>();

    static {
        JOB_RUNNER_MAP.put("aType", new JobRunnerA()); // 也可以从Spring中拿
        JOB_RUNNER_MAP.put("bType", new JobRunnerB());
    }

    @Override
    public Result run(Job job) throws Throwable {
        String type = job.getParam("type");
        return JOB_RUNNER_MAP.get(type).run(job);
    }
}

class JobRunnerA implements JobRunner {
    @Override
    public Result run(Job job) throws Throwable {
        //  TODO A类型Job的逻辑
        return null;
    }
}

class JobRunnerB implements JobRunner {
    @Override
    public Result run(Job job) throws Throwable {
        // TODO B类型Job的逻辑
        return null;
    }
}