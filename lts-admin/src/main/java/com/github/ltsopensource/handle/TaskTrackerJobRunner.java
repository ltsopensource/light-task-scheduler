package com.github.ltsopensource.handle;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class TaskTrackerJobRunner implements JobRunner {

    private static final Logger logger = LoggerFactory.getLogger(TaskTrackerJobRunner.class);

    @Override
    public Result run(JobContext ctx) throws Throwable {
        try {
//            BizLogger bizLogger = LtsLoggerFactory.getBizLogger();
//            BizLogger log = ctx.getBizLogger();
            Job job = ctx.getJob();
            // TODO 业务逻辑
            logger.info("Job Context:" + ctx);
            // 会发送到 LTS (JobTracker上)
            System.out.println("\n\n\n---------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("-admin-task-tracker--"+job.getTaskId()+"-->"+job.getTaskTrackerNodeGroup()+"-->"+job.getSubmitNodeGroup());
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------\n\n\n");
        } catch (Exception e) {
        	logger.info("Admin Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "Admin Excute Success!");
    }
}
