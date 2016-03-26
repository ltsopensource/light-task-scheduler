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

    private ConcurrentMap<String, QuartzJob> JOB_MAP = new ConcurrentHashMap<String, QuartzJob>();

    public QuartzJobRunnerDispatcher(List<QuartzJob> quartzJobs) {
        for (QuartzJob quartzJob : quartzJobs) {
            String name = quartzJob.getName();
            JOB_MAP.put(name, quartzJob);
        }
    }

    @Override
    public Result run(Job job) throws Throwable {
        String taskId = job.getTaskId();

        QuartzJob quartzJob = JOB_MAP.get(taskId);
        if (quartzJob == null) {
            return new Result(Action.EXECUTE_FAILED, "Can't find the taskId[" + taskId + "]'s QuartzCronJob");
        }

        quartzJob.getMethodInvoker().invoke();

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
