package com.lts.job.example.support;

import com.lts.job.core.domain.Job;
import com.lts.job.task.tracker.logger.BizLogger;
import com.lts.job.task.tracker.runner.JobRunner;
import com.lts.job.task.tracker.runner.LtsLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 */
public class TestJobRunner implements JobRunner {

    @Override
    public void run(Job job) throws Throwable {

        System.out.println(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                        + " 我要执行：" + job + "shopId=" + job.getParam("shopId"));

        BizLogger bizLogger = LtsLogger.getBizLogger();
        bizLogger.info("测试，业务日志啊啊啊啊啊");

        try {
            Thread.sleep(5*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
