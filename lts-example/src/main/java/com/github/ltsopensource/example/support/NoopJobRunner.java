package com.github.ltsopensource.example.support;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.support.SystemClock;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
public class NoopJobRunner implements JobRunner {

    static volatile long start = 0;
    static AtomicInteger num = new AtomicInteger(0);

    public NoopJobRunner() {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
    }

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        System.out.println(num.incrementAndGet() + "  time : " + (SystemClock.now() - start) + "ms");
        return new Result(Action.EXECUTE_SUCCESS);
    }
}
