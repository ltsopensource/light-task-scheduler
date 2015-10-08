package com.lts.example.support;

import com.lts.core.domain.Action;
import com.lts.core.domain.Job;
import com.lts.core.logger.Logger;
import com.lts.core.logger.LoggerFactory;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.logger.BizLogger;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.LtsLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class SpringXmlJobRunner implements JobRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringXmlJobRunner.class);

    SpringBean springBean;

    @Override
    public Result run(Job job) throws Throwable {
        try {
            Thread.sleep(1000L);

            springBean.hello();

            // TODO 业务逻辑
            LOGGER.info("我要执行：" + job);
            BizLogger bizLogger = LtsLoggerFactory.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }

    public void setSpringBean(SpringBean springBean) {
        this.springBean = springBean;
    }
}
