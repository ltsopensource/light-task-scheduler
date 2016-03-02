/**
 * Created by 28797575@qq.com hongliangpan on 2016/2/29.
 */
package com.lts.tasktracker.jobdispatcher;


// 作业开发部署
/*
自动扫描作业，并派发[根据注解的type参数进行派发]

1. 配置JobRunnerDispatcher为JobRunner
tasktracker.cfg文件中
# JobRunner 任务执行类
jobRunnerClass=com.lts.tasktracker.jobdispatcher.JobRunnerDispatcher

2. 开发新的作业
//添加 类注解 @RunnerTask(type= "type")
@JobRunnerAnnotation(type= "type1")
public class GatherMetricJobRunner implements JobRunner

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
5. 配置参数

# JobRunner 任务执行类
jobRunnerClass=com.lts.tasktracker.jobdispatcher.JobRunnerDispatcher

# 自动扫描作业包，逗号分隔
jobRunnerScannerPackages=com.glodon.ysg,com.lts.job

# 是否内嵌JobClient
isEmbedJobClient=true

# 提交作业类，初始化一些作业，可以通过admin-web修改作业信息
jobSubmitterClass=com.lts.tasktracker.jobdispatcher.UbaJobSubmitter
# 自定义的cron，可以通过admin-web修改作业信息
mySubmitCronExpression=0 53/10 2-3 * * ?
*/