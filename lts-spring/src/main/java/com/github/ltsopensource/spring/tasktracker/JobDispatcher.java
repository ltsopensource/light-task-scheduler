package com.github.ltsopensource.spring.tasktracker;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import org.springframework.util.StringUtils;

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
        if (!StringUtils.isEmpty(value)) {
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
        if (!StringUtils.isEmpty(shardField)) {
            this.shardField = shardField;
        }
    }

}
