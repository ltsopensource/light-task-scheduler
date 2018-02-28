package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.cluster.LTSConfig;
import com.github.ltsopensource.core.constant.Environment;
import com.github.ltsopensource.core.constant.Level;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.logger.BizLoggerFactory;

/**
 * 为了方便JobRunner测试设计的
 *
 * @author Robert HG (254963746@qq.com) on 9/13/15.
 */
public abstract class JobRunnerTester {

    public Result run(JobContext jobContext) throws Throwable {
        // 1. 设置LTS环境为 UNIT_TEST
        LTSConfig.setEnvironment(Environment.UNIT_TEST);
        // 设置 BizLogger
        jobContext.setBizLogger(BizLoggerFactory.getLogger(Level.INFO, null, null));
        // 2. load context (Spring Context 或者其他的)
        initContext();
        // 3. new jobRunner
        JobRunner jobRunner = newJobRunner();
        // 4. run job
        return jobRunner.run(jobContext);
    }

    /**
     * 初始化上下文 (Spring Context等),准备运行环境
     */
    protected abstract void initContext();

    /**
     * 创建JobRunner
     */
    protected abstract JobRunner newJobRunner();

}
