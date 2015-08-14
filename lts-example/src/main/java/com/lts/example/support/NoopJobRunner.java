package com.lts.example.support;

import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.core.support.SystemClock;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
public class NoopJobRunner implements JobRunner {

    static long start = 0;
    static AtomicInteger num = new AtomicInteger(0);

    public NoopJobRunner() {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
    }

    @Override
    public Result run(Job job) throws Throwable {
        System.out.println(num.incrementAndGet() + "  time : " + (SystemClock.now() - start) + "ms");
        return new Result(Action.EXECUTE_SUCCESS);
    }
}
