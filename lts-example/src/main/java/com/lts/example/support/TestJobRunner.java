package com.lts.example.support;

import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.core.support.SystemClock;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.logger.BizLogger;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.LtsLoggerFactory;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TestJobRunner implements JobRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestJobRunner.class);
    private final static BizLogger bizLogger = LtsLoggerFactory.getBizLogger();

    @Override
    public Result run(Job job) throws Throwable {
        try {
//            Thread.sleep(1000L);
//
//            if (job.getRetryTimes() > 5) {
//                return new Result(Action.EXECUTE_FAILED, "重试次数超过5次了，放过你吧!");
//            }
//
//            if (SystemClock.now() % 2 == 1) {
//                return new Result(Action.EXECUTE_LATER, "稍后执行");
//            }

            // TODO 业务逻辑
            LOGGER.info("我要执行：" + job);
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}
