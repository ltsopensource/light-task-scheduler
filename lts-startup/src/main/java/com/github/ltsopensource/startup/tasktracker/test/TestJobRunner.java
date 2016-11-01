package com.github.ltsopensource.startup.tasktracker.test;

import com.github.ltsopensource.core.commons.utils.DateUtils;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class TestJobRunner implements JobRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestJobRunner.class);
    private static AtomicLong counter = new AtomicLong(0);
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            LOGGER.error(DateUtils.formatYMD_HMS(new Date()) + "   " + counter.incrementAndGet());
        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}