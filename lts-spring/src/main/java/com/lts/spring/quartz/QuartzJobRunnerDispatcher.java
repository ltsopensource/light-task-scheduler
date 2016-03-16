package com.lts.spring.quartz;

import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Robert HG (254963746@qq.com) on 3/16/16.
 */
class QuartzJobRunnerDispatcher implements JobRunner {

    private ConcurrentMap<String, QuartzCronJob> JOB_MAP = new ConcurrentHashMap<String, QuartzCronJob>();

    public QuartzJobRunnerDispatcher(List<QuartzCronJob> quartzCronJobs) {
        for (QuartzCronJob quartzCronJob : quartzCronJobs) {
            String name = quartzCronJob.getCronTrigger().getName();
            JOB_MAP.put(name, quartzCronJob);
        }
    }

    @Override
    public Result run(Job job) throws Throwable {
        String taskId = job.getTaskId();

        QuartzCronJob quartzCronJob = JOB_MAP.get(taskId);
        if (quartzCronJob == null) {
            return new Result(Action.EXECUTE_FAILED, "Can't find the taskId[" + taskId + "]'s QuartzCronJob");
        }

        quartzCronJob.getMethodInvoker().invoke();

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
