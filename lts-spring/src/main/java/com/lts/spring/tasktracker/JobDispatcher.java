package com.lts.spring.tasktracker;

import com.lts.core.commons.utils.StringUtils;
import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobContext;
import com.lts.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
public class JobDispatcher implements JobRunner {

    private String shardField = "taskId";

    @Override
    public Result run(JobContext jobContext) throws Throwable {

        Job job = jobContext.getJob();

        String value;
        if (shardField.equals("taskId")) {
            value = job.getTaskId();
        } else {
            value = job.getParam(shardField);
        }

        JobRunner jobRunner = null;
        if (StringUtils.isNotEmpty(value)) {
            jobRunner = JobRunnerHolder.getJobRunner(value);
        }
        if (jobRunner == null) {
            jobRunner = JobRunnerHolder.getJobRunner("_LTS_DEFAULT");

            if (jobRunner == null) {
                throw new JobDispatchException("Can not find JobRunner by Shard Value : [" + value + "]");
            }
        }
        return jobRunner.run(jobContext);
    }

    public void setShardField(String shardField) {
        if (StringUtils.isNotEmpty(shardField)) {
            this.shardField = shardField;
        }
    }

}
