package com.github.ltsopensource.startup.tasktracker.test;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
public class TestJobRunner implements JobRunner {

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        System.out.println(JSON.toJSONString(jobContext));

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
