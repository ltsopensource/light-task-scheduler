package com.lts.startup.test;

import com.lts.core.json.JSON;
import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 9/12/15.
 */
public class TestJobRunner implements JobRunner {

    @Override
    public Result run(Job job) throws Throwable {
        System.out.println(JSON.toJSONString(job));

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
