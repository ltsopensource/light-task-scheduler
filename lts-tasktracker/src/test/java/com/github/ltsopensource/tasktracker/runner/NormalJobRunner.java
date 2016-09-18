package com.github.ltsopensource.tasktracker.runner;

import com.github.ltsopensource.core.json.JSON;
import com.github.ltsopensource.tasktracker.Result;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public class NormalJobRunner implements JobRunner {

    protected boolean stop = false;

    public static AtomicLong l = new AtomicLong(0);

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        System.out.println("我开始执行:" + JSON.toJSONString(jobContext));
        while (!stop) {
            l.incrementAndGet();
        }
        System.out.println("我退出了");
        return null;
    }
}
