package com.github.ltsopensource.spring.quartz;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzJobRunnerDispatcher implements JobRunner {

    private ConcurrentMap<String, QuartzJobContext> JOB_MAP = new ConcurrentHashMap<String, QuartzJobContext>();

    public QuartzJobRunnerDispatcher(List<QuartzJobContext> quartzJobContexts) {
        for (QuartzJobContext quartzJobContext : quartzJobContexts) {
            String name = quartzJobContext.getName();
            JOB_MAP.put(name, quartzJobContext);
        }
    }

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();
        String taskId = job.getTaskId();

        QuartzJobContext quartzJobContext = JOB_MAP.get(taskId);
        if (quartzJobContext == null) {
            return new Result(Action.EXECUTE_FAILED, "Can't find the taskId[" + taskId + "]'s QuartzCronJob");
        }

        quartzJobContext.getJobExecution().execute(quartzJobContext, job);

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
