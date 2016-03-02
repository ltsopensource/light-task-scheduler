/**
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
package com.lts.example.jobdispatcher;


// 作业开发部署
/*
自动扫描作业，并派发
1. 配置JobRunnerDispatcher为JobRunner
tasktracker.cfg文件中
# JobRunner 任务执行类
jobRunnerClass=com.glodon.ysg.uba.job.JobRunnerDispatcher

2. 开发新的作业
//添加 类注解 @RunnerTask(type= "type")
@JobRunnerAnnotation(type= "type1")
public class GatherFunctionJobRunner implements JobRunner

3.自动扫描作业JobRunnerDispatcher,配置扫描的package
 static {
        try {
            JobRunnerScanner.scan("com.glodon.ysg", JOB_RUNNER_MAP);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
4. 自行开发的作业，把jar包，放入tasktracker\lib
重启tasktracker，就自动运行
*/