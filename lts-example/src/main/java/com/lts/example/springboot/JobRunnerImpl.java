package com.lts.example.springboot;

import com.lts.core.domain.Action;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.spring.boot.annotation.JobRunner4TaskTracker;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.logger.BizLogger;
import com.lts.tasktracker.runner.JobContext;
import com.lts.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 4/9/16.
 */
@JobRunner4TaskTracker
public class JobRunnerImpl implements JobRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunnerImpl.class);

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            BizLogger bizLogger = jobContext.getBizLogger();

            // TODO 业务逻辑
            LOGGER.info("我要执行：" + jobContext);
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }
}
