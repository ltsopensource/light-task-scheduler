package com.github.ltsopensource.startup.tasktracker.test;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.log4j.Log4j2;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
@Log4j2
public class TestJobRunner implements JobRunner {

    private static AtomicLong counter = new AtomicLong(0);

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            log.error(new Date() + "   " + counter.incrementAndGet());
        } catch (Exception e) {
            log.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}
