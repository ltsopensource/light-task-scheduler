package com.lts.example.support;

import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.logger.BizLogger;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.LtsLoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TestJobRunner implements JobRunner {

    @Override
    public Result run(Job job) throws Throwable {
        try {
            System.out.println(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                            + " 我要执行：" + job + "shopId=" + job.getParam("shopId"));

            BizLogger bizLogger = LtsLoggerFactory.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

            Thread.sleep(1000L);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }
        return new Result(true, "执行成功了，哈哈");
    }
}
