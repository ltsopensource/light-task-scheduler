package com.lts.example.support;

import com.lts.core.json.JSON;
import com.lts.core.domain.Job;
import com.lts.tasktracker.Result;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.JobRunnerTester;

/**
 * @author Robert HG (254963746@qq.com) on 9/13/15.
 */
public class TestJobRunnerTester extends JobRunnerTester {

    public static void main(String[] args) throws Throwable {
        //  Mock Job 数据
        Job job = new Job();
        job.setTaskId("2313213");
        // 运行测试
        TestJobRunnerTester tester = new TestJobRunnerTester();
        Result result = tester.run(job);
        System.out.println(JSON.toJSONString(result));
    }

    @Override
    protected void initContext() {
        // TODO 初始化Spring容器
    }

    @Override
    protected JobRunner newJobRunner() {
        return new TestJobRunner();
    }
}
