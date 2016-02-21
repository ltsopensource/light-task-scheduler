package com.lts.tasktracker.runner;

import com.lts.core.domain.Job;
import com.lts.core.json.JSON;
import com.lts.tasktracker.Result;

/**
 * @author Robert HG (254963746@qq.com) on 2/21/16.
 */
public class TestInterruptorJobRunner implements InterruptibleJobRunner {

    private boolean stop = false;

    @Override
    public void interrupt() {
        System.out.println("我设置停止标识");
        stop = true;
    }

    @Override
    public Result run(Job job) throws Throwable {
        System.out.println("我开始执行:" + JSON.toJSONString(job));
        while (!stop) {
            int i = 1;
        }
        System.out.println("我退出了");
        return null;
    }
}
