package com.lts.tasktracker.jobdispatcher;

import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;

/**
 * Created by hugui.hg on 3/2/16.
 */
@JobRunnerAnnotation(type= "type1")
public class GatherMetricJobRunner implements JobRunner {
    @Override
    public Result run(Job job) throws Throwable {
        return null;
    }
}
